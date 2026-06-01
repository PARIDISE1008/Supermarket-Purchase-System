package com.supermarket.service.impl;

import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.SupplierMapper;
import com.supermarket.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    @Override
    public void add(Supplier supplier) {
        validateSupplier(supplier);
        Supplier existing = supplierMapper.selectByPhone(supplier.getPhone());
        if (existing != null) {
            throw BusinessException.duplicate("供应商电话");
        }
        int rows = supplierMapper.insert(supplier);
        if (rows == 0) {
            throw BusinessException.operationFailed("新增供应商失败");
        }
        System.out.println("新增供应商成功，ID=" + supplier.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Supplier> suppliers) {
        if (suppliers == null || suppliers.isEmpty()) {
            throw BusinessException.paramError("导入数据不能为空");
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < suppliers.size(); i++) {
            try {
                validateSupplier(suppliers.get(i));
            } catch (BusinessException e) {
                errors.add(String.format("第%d行: %s", i + 1, e.getMessage()));
            }
        }
        if (!errors.isEmpty()) {
            throw BusinessException.paramError("数据校验失败：" + String.join("; ", errors));
        }
        for (int i = 0; i < suppliers.size(); i++) {
            for (int j = i + 1; j < suppliers.size(); j++) {
                if (suppliers.get(i).getPhone().equals(suppliers.get(j).getPhone())) {
                    throw BusinessException.paramError(
                        String.format("第%d行和第%d行电话重复", i + 1, j + 1));
                }
            }
        }
        for (Supplier supplier : suppliers) {
            Supplier existing = supplierMapper.selectByPhone(supplier.getPhone());
            if (existing != null) {
                throw BusinessException.duplicate("电话 " + supplier.getPhone());
            }
        }
        supplierMapper.batchInsert(suppliers);
        System.out.println("批量导入供应商成功，共" + suppliers.size() + "条");
    }

    @Override
    public Result<List<Supplier>> search(String name, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int offset = (page - 1) * size;
        List<Supplier> list = supplierMapper.selectPage(name, offset, size);
        int total = supplierMapper.count(name);
        return Result.success("查询成功", list, total);
    }

    @Override
    public Supplier getById(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("供应商ID不合法");
        }
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw BusinessException.notFound("供应商");
        }
        return supplier;
    }

    @Override
    public void update(Supplier supplier) {
        if (supplier.getId() == null || supplier.getId() <= 0) {
            throw BusinessException.paramError("供应商ID不合法");
        }
        validateSupplier(supplier);
        Supplier existing = supplierMapper.selectById(supplier.getId());
        if (existing == null) {
            throw BusinessException.notFound("供应商");
        }
        Supplier phoneExists = supplierMapper.selectByPhone(supplier.getPhone());
        if (phoneExists != null && !phoneExists.getId().equals(supplier.getId())) {
            throw BusinessException.duplicate("供应商电话");
        }
        int rows = supplierMapper.update(supplier);
        if (rows == 0) {
            throw BusinessException.operationFailed("修改供应商失败");
        }
        System.out.println("修改供应商成功，ID=" + supplier.getId());
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("供应商ID不合法");
        }
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw BusinessException.notFound("供应商");
        }
        int goodsCount = supplierMapper.countGoodsBySupplierId(id);
        if (goodsCount > 0) {
            throw new BusinessException(
                String.format("该供应商下还有%d件商品，请先处理商品后再删除", goodsCount));
        }
        int rows = supplierMapper.deleteById(id);
        if (rows == 0) {
            throw BusinessException.operationFailed("删除供应商失败");
        }
        System.out.println("删除供应商成功，ID=" + id);
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