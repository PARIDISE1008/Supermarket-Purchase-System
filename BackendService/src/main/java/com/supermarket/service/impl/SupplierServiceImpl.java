package com.supermarket.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.SupplierMapper;
import com.supermarket.service.SupplierService;

@Service
public class SupplierServiceImpl implements SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierServiceImpl.class);

    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    @Override
    public void add(Supplier supplier) {
        validateSupplier(supplier);

        Supplier existing = supplierMapper.selectByPhone(supplier.getPhone());
        if (existing != null) {
            log.warn("[新增供应商失败] 电话重复 phone={}", supplier.getPhone());
            throw BusinessException.duplicate("供应商电话");
        }

        int rows = supplierMapper.insert(supplier);
        if (rows == 0) {
            log.error("[新增供应商失败] 数据库插入返回0 name={}", supplier.getName());
            throw BusinessException.operationFailed("新增供应商失败");
        }

        log.info("[新增供应商成功] id={}, name={}", supplier.getId(), supplier.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Supplier> suppliers) {
        if (suppliers == null || suppliers.isEmpty()) {
            log.warn("[批量导入失败] 数据为空");
            throw BusinessException.paramError("导入数据不能为空");
        }

        log.info("[批量导入开始] 总数={}", suppliers.size());

        // 一遍循环完成：校验 + 内部查重 + 收集电话
        List<String> errors = new ArrayList<>();
        Set<String> phoneSet = new HashSet<>();
        List<String> allPhones = new ArrayList<>();

        for (int i = 0; i < suppliers.size(); i++) {
            Supplier s = suppliers.get(i);
            try {
                validateSupplier(s);
                if (!phoneSet.add(s.getPhone())) {
                    errors.add(String.format("第%d行: 电话 %s 在导入数据中重复", i + 1, s.getPhone()));
                }
                allPhones.add(s.getPhone());
            } catch (BusinessException e) {
                errors.add(String.format("第%d行: %s", i + 1, e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            log.warn("[批量导入失败] 校验不通过 errors={}", errors);
            throw BusinessException.paramError("数据校验失败：" + String.join("; ", errors));
        }

        // 一次 IN 查询替代 N 次单查
        List<Supplier> dbExists = supplierMapper.selectByPhones(allPhones);
        if (!dbExists.isEmpty()) {
            log.warn("[批量导入失败] 电话已存在 phone={}", dbExists.get(0).getPhone());
            throw BusinessException.duplicate("电话 " + dbExists.get(0).getPhone());
        }

        try {
            supplierMapper.batchInsert(suppliers);
            log.info("[批量导入成功] 共{}条", suppliers.size());
        } catch (Exception e) {
            log.error("[批量导入失败] 数据库异常，事务已回滚", e);
            throw new BusinessException("批量导入失败，数据已回滚");
        }
    }

    @Override
    public Result<List<Supplier>> search(String name, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        int offset = (page - 1) * size;
        List<Supplier> list = supplierMapper.selectPage(name, offset, size);
        int total = supplierMapper.count(name);

        log.debug("[查询供应商] name={}, page={}, size={}, total={}", name, page, size, total);
        return Result.success("查询成功", list, total);
    }

    @Override
    public Supplier getById(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[查询供应商失败] 非法ID id={}", id);
            throw BusinessException.paramError("供应商ID不合法");
        }

        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            log.warn("[查询供应商失败] 不存在 id={}", id);
            throw BusinessException.notFound("供应商");
        }

        return supplier;
    }

    @Override
    public void update(Supplier supplier) {
        if (supplier.getId() == null || supplier.getId() <= 0) {
            log.warn("[修改供应商失败] 非法ID id={}", supplier.getId());
            throw BusinessException.paramError("供应商ID不合法");
        }
        validateSupplier(supplier);

        Supplier existing = supplierMapper.selectById(supplier.getId());
        if (existing == null) {
            log.warn("[修改供应商失败] 不存在 id={}", supplier.getId());
            throw BusinessException.notFound("供应商");
        }

        Supplier phoneExists = supplierMapper.selectByPhone(supplier.getPhone());
        if (phoneExists != null && !phoneExists.getId().equals(supplier.getId())) {
            log.warn("[修改供应商失败] 电话重复 id={}, phone={}", supplier.getId(), supplier.getPhone());
            throw BusinessException.duplicate("供应商电话");
        }

        int rows = supplierMapper.update(supplier);
        if (rows == 0) {
            log.error("[修改供应商失败] 数据库更新返回0 id={}", supplier.getId());
            throw BusinessException.operationFailed("修改供应商失败");
        }

        log.info("[修改供应商成功] id={}, name={}", supplier.getId(), supplier.getName());
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[删除供应商失败] 非法ID id={}", id);
            throw BusinessException.paramError("供应商ID不合法");
        }

        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            log.warn("[删除供应商失败] 不存在 id={}", id);
            throw BusinessException.notFound("供应商");
        }

        int goodsCount = supplierMapper.countGoodsBySupplierId(id);
        if (goodsCount > 0) {
            log.warn("[删除供应商失败] 存在关联商品 id={}, goodsCount={}", id, goodsCount);
            throw new BusinessException(
                    String.format("该供应商下还有%d件商品，请先处理商品后再删除", goodsCount));
        }

        int rows = supplierMapper.deleteById(id);
        if (rows == 0) {
            log.error("[删除供应商失败] 数据库更新返回0 id={}", id);
            throw BusinessException.operationFailed("删除供应商失败");
        }

        log.info("[删除供应商成功] id={}, name={}", id, supplier.getName());
    }

    private void validateSupplier(Supplier supplier) {
        if (supplier == null) {
            throw BusinessException.paramError("供应商信息不能为空");
        }
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw BusinessException.paramError("供应商名称不能为空");
        }
        if (supplier.getName().length() > 100) {
            throw BusinessException.paramError("供应商名称长度不能超过100");
        }
        if (supplier.getPhone() == null || supplier.getPhone().trim().isEmpty()) {
            throw BusinessException.paramError("供应商电话不能为空");
        }
        if (!supplier.getPhone().matches("^[0-9\\-]{7,20}$")) {
            throw BusinessException.paramError("电话格式不正确");
        }
        if (supplier.getEmail() != null && !supplier.getEmail().isEmpty()
                && !supplier.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw BusinessException.paramError("邮箱格式不正确");
        }
        if (supplier.getRemark() != null && supplier.getRemark().length() > 500) {
            throw BusinessException.paramError("备注长度不能超过500");
        }
    }
}
