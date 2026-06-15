package com.supermarket.mapper;

import com.supermarket.entity.PurchaseDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PurchaseDetailMapper {

    int insert(PurchaseDetail detail);

    int batchInsert(@Param("list") List<PurchaseDetail> list);

    List<PurchaseDetail> selectByMainId(@Param("purchaseMainId") Integer purchaseMainId);

    int deleteByMainId(@Param("purchaseMainId") Integer purchaseMainId);
}
