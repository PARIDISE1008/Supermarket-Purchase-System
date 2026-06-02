package com.supermarket.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.common.Result;
import com.supermarket.entity.Goods;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.GoodsMapper;
import com.supermarket.mapper.SupplierMapper;
import com.supermarket.service.GoodsService;

@Service
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final SupplierMapper supplierMapper;

    public GoodsServiceImpl(GoodsMapper goodsMapper, SupplierMapper supplierMapper) {
        this.goodsMapper = goodsMapper;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public void add(Goods goods) {
        validateGoods(goods);

        // 检查供应商是否存在
        if (supplierMapper.selectById(goods.getSupplierId()) == null) {
            throw BusinessException.notFound("供应商");
        }

        // 检查同供应商下商品名是否重复
        Goods existing = goodsMapper.selectBySupplierAndName(goods.getSupplierId(), goods.getName());
        if (existing != null) {
            throw BusinessException.duplicate("该供应商下商品名称");
        }

        int rows = goodsMapper.insert(goods);
        if (rows == 0) {
            throw BusinessException.operationFailed("新增商品失败");
        }

        System.out.println("新增商品成功，ID=" + goods.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Goods> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            throw BusinessException.paramError("导入数据不能为空");
        }

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
            throw BusinessException.paramError("数据校验失败：" + String.join("; ", errors));
        }

        goodsMapper.batchInsert(goodsList);
        System.out.println("批量导入商品成功，共" + goodsList.size() + "条");
    }

    @Override
    public Result<List<Goods>> search(String name, Integer supplierId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.selectPage(name, supplierId, offset, size);
        int total = goodsMapper.count(name, supplierId);

        return Result.success("查询成功", list, total);
    }

    @Override
    public Goods getById(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("商品ID不合法");
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw BusinessException.notFound("商品");
        }

        return goods;
    }

    @Override
    public void update(Goods goods) {
        if (goods.getId() == null || goods.getId() <= 0) {
            throw BusinessException.paramError("商品ID不合法");
        }
        validateGoods(goods);

        Goods existing = goodsMapper.selectById(goods.getId());
        if (existing == null) {
            throw BusinessException.notFound("商品");
        }

        if (supplierMapper.selectById(goods.getSupplierId()) == null) {
            throw BusinessException.notFound("供应商");
        }

        // 查重：同供应商下同名商品（排除自己）
        Goods duplicate = goodsMapper.selectBySupplierAndName(goods.getSupplierId(), goods.getName());
        if (duplicate != null && !duplicate.getId().equals(goods.getId())) {
            throw BusinessException.duplicate("该供应商下商品名称");
        }

        int rows = goodsMapper.update(goods);
        if (rows == 0) {
            throw BusinessException.operationFailed("修改商品失败");
        }

        System.out.println("修改商品成功，ID=" + goods.getId());
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("商品ID不合法");
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw BusinessException.notFound("商品");
        }

        // 检查是否有采购明细引用
        int detailCount = goodsMapper.countPurchaseDetailByGoodsId(id);
        if (detailCount > 0) {
            throw new BusinessException(
                String.format("该商品已被%d条采购记录引用，无法删除", detailCount));
        }

        int rows = goodsMapper.deleteById(id);
        if (rows == 0) {
            throw BusinessException.operationFailed("删除商品失败");
        }

        System.out.println("删除商品成功，ID=" + id);
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
