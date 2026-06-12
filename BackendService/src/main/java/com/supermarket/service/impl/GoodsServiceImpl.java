package com.supermarket.service.impl;

import com.supermarket.common.Result;
import com.supermarket.entity.Goods;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.GoodsMapper;
import com.supermarket.mapper.SupplierMapper;
import com.supermarket.service.GoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    private static final Logger log = LoggerFactory.getLogger(GoodsServiceImpl.class);

    private final GoodsMapper goodsMapper;
    private final SupplierMapper supplierMapper;

    public GoodsServiceImpl(GoodsMapper goodsMapper, SupplierMapper supplierMapper) {
        this.goodsMapper = goodsMapper;
        this.supplierMapper = supplierMapper;
    }

    @Override
    @CacheEvict(value = "goods", allEntries = true)
    public void add(Goods goods) {
        validateGoods(goods);

        if (supplierMapper.selectById(goods.getSupplierId()) == null) {
            log.warn("[新增商品失败] 供应商不存在 supplierId={}", goods.getSupplierId());
            throw BusinessException.notFound("供应商");
        }

        Goods existing = goodsMapper.selectBySupplierAndName(goods.getSupplierId(), goods.getName());
        if (existing != null) {
            log.warn("[新增商品失败] 同供应商下名称重复 supplierId={}, name={}", goods.getSupplierId(), goods.getName());
            throw BusinessException.duplicate("该供应商下商品名称");
        }

        int rows = goodsMapper.insert(goods);
        if (rows == 0) {
            log.error("[新增商品失败] 数据库插入返回0 name={}", goods.getName());
            throw BusinessException.operationFailed("新增商品失败");
        }

        log.info("[新增商品成功] id={}, name={}, price={}", goods.getId(), goods.getName(), goods.getPrice());
    }

    @Override
    @CacheEvict(value = "goods", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Goods> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            log.warn("[批量导入商品失败] 数据为空");
            throw BusinessException.paramError("导入数据不能为空");
        }

        log.info("[批量导入商品开始] 总数={}", goodsList.size());

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < goodsList.size(); i++) {
            try {
                validateGoods(goodsList.get(i));
                if (supplierMapper.selectById(goodsList.get(i).getSupplierId()) == null) {
                    errors.add(String.format("第%d行: 供应商不存在", i + 1));
                }
            } catch (BusinessException e) {
                errors.add(String.format("第%d行: %s", i + 1, e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            log.warn("[批量导入商品失败] 校验不通过 errors={}", errors);
            throw BusinessException.paramError("数据校验失败：" + String.join("; ", errors));
        }

        try {
            goodsMapper.batchInsert(goodsList);
            log.info("[批量导入商品成功] 共{}条", goodsList.size());
        } catch (Exception e) {
            log.error("[批量导入商品失败] 数据库异常，事务已回滚", e);
            throw new BusinessException("批量导入失败，数据已回滚");
        }
    }

    @Override
    @Cacheable(value = "goods", key = "'search:' + (#name != null ? #name : '~') + ':' + (#supplierId != null ? #supplierId : '~') + ':' + #page + ':' + #size")
    public Result<List<Goods>> search(String name, Integer supplierId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.selectPage(name, supplierId, offset, size);
        int total = goodsMapper.count(name, supplierId);

        log.debug("[查询商品] name={}, supplierId={}, page={}, total={}", name, supplierId, page, total);
        return Result.success("查询成功", list, total);
    }

    @Override
    @Cacheable(value = "goods", key = "#id")
    public Goods getById(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[查询商品失败] 非法ID id={}", id);
            throw BusinessException.paramError("商品ID不合法");
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            log.warn("[查询商品失败] 不存在 id={}", id);
            throw BusinessException.notFound("商品");
        }

        return goods;
    }

    @Override
    @CacheEvict(value = "goods", allEntries = true)
    public void update(Goods goods) {
        if (goods.getId() == null || goods.getId() <= 0) {
            log.warn("[修改商品失败] 非法ID id={}", goods.getId());
            throw BusinessException.paramError("商品ID不合法");
        }
        validateGoods(goods);

        Goods existing = goodsMapper.selectById(goods.getId());
        if (existing == null) {
            log.warn("[修改商品失败] 不存在 id={}", goods.getId());
            throw BusinessException.notFound("商品");
        }

        if (supplierMapper.selectById(goods.getSupplierId()) == null) {
            log.warn("[修改商品失败] 供应商不存在 supplierId={}", goods.getSupplierId());
            throw BusinessException.notFound("供应商");
        }

        Goods duplicate = goodsMapper.selectBySupplierAndName(goods.getSupplierId(), goods.getName());
        if (duplicate != null && !duplicate.getId().equals(goods.getId())) {
            log.warn("[修改商品失败] 同供应商下名称重复 id={}, supplierId={}, name={}", goods.getId(), goods.getSupplierId(), goods.getName());
            throw BusinessException.duplicate("该供应商下商品名称");
        }

        int rows = goodsMapper.update(goods);
        if (rows == 0) {
            log.error("[修改商品失败] 数据库更新返回0 id={}", goods.getId());
            throw BusinessException.operationFailed("修改商品失败");
        }

        log.info("[修改商品成功] id={}, name={}, price={}", goods.getId(), goods.getName(), goods.getPrice());
    }

    @Override
    @CacheEvict(value = "goods", allEntries = true)
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[删除商品失败] 非法ID id={}", id);
            throw BusinessException.paramError("商品ID不合法");
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            log.warn("[删除商品失败] 不存在 id={}", id);
            throw BusinessException.notFound("商品");
        }

        int detailCount = goodsMapper.countPurchaseDetailByGoodsId(id);
        if (detailCount > 0) {
            log.warn("[删除商品失败] 存在采购记录引用 id={}, detailCount={}", id, detailCount);
            throw new BusinessException(
                    String.format("该商品已被%d条采购记录引用，无法删除", detailCount));
        }

        int rows = goodsMapper.deleteById(id);
        if (rows == 0) {
            log.error("[删除商品失败] 数据库更新返回0 id={}", id);
            throw BusinessException.operationFailed("删除商品失败");
        }

        log.info("[删除商品成功] id={}, name={}", id, goods.getName());
    }

    private void validateGoods(Goods goods) {
        if (goods == null) {
            throw BusinessException.paramError("商品信息不能为空");
        }
        if (goods.getName() == null || goods.getName().trim().isEmpty()) {
            throw BusinessException.paramError("商品名称不能为空");
        }
        if (goods.getName().length() > 100) {
            throw BusinessException.paramError("商品名称长度不能超过100");
        }
        if (goods.getPrice() == null) {
            throw BusinessException.paramError("商品单价不能为空");
        }
        if (goods.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.paramError("商品单价不能为负数");
        }
        if (goods.getSupplierId() == null) {
            throw BusinessException.paramError("供应商不能为空");
        }
    }
}
