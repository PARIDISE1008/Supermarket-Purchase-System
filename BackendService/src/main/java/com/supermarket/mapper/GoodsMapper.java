package com.supermarket.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.supermarket.entity.Goods;

@Mapper
public interface GoodsMapper {

    int insert(Goods goods);

    int batchInsert(@Param("list") List<Goods> list);

    Goods selectById(@Param("id") Integer id);

    /**
     * 检查同一供应商下是否有同名商品（用于新增/修改查重）
     */
    Goods selectBySupplierAndName(@Param("supplierId") Integer supplierId,
                                   @Param("name") String name);

    /**
     * 分页查询（关联供应商名）
     */
    List<Goods> selectPage(@Param("name") String name,
                            @Param("supplierId") Integer supplierId,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    int count(@Param("name") String name,
              @Param("supplierId") Integer supplierId);

    int update(Goods goods);

    int deleteById(@Param("id") Integer id);

    /**
     * 查询该商品被多少采购明细引用（删除前检查）
     */
    int countPurchaseDetailByGoodsId(@Param("goodsId") Integer goodsId);
}
