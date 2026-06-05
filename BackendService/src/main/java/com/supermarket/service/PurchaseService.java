package com.supermarket.service;

import java.util.List;

import com.supermarket.common.Result;
import com.supermarket.dto.PurchaseSubmitDTO;
import com.supermarket.entity.PurchaseMain;

public interface PurchaseService {

    /**
     * 预生成订单号（进入页面时调用）
     */
    Result<String> generateOrderNo();

    /**
     * 提交采购单
     */
    Result<String> submit(PurchaseSubmitDTO dto);

    /**
     * 查自己的订单列表（按状态分Tab）
     */
    Result<List<PurchaseMain>> listMyOrders(Integer employeeId, String status, Integer page, Integer size);

    /**
     * 查订单详情（含明细）
     */
    Result<PurchaseMain> getDetail(Integer orderId);

    /**
     * 作废订单（DRAFT → CANCELLED）
     */
    Result<Void> cancel(Integer orderId, Integer employeeId);

    /**
     * 恢复订单（CANCELLED → DRAFT，截止前）
     */
    Result<Void> restore(Integer orderId, Integer employeeId);
}
