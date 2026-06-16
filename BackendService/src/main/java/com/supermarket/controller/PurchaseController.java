package com.supermarket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supermarket.common.Result;
import com.supermarket.dto.PurchaseSubmitDTO;
import com.supermarket.entity.PurchaseDetail;
import com.supermarket.entity.PurchaseMain;
import com.supermarket.service.PurchaseService;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    /**
     * 预生成订单号
     */
    @GetMapping("/order-no")
    public Result<String> generateOrderNo() {
        return purchaseService.generateOrderNo();
    }

    /**
     * 提交采购单
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody PurchaseSubmitDTO dto) {
        return purchaseService.submit(dto);
    }

    /**
     * 查自己的订单列表（按状态分Tab）
     */
    @GetMapping("/my")
    public Result<List<PurchaseMain>> listMyOrders(
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return purchaseService.listMyOrders(employeeId, status, page, size);
    }

    /**
     * 查订单详情
     */
    @GetMapping("/{id}")
    public Result<PurchaseMain> getDetail(@PathVariable Integer id) {
        return purchaseService.getDetail(id);
    }

    /**
     * 查订单明细
     */
    @GetMapping("/{id}/details")
    public Result<List<PurchaseDetail>> getDetails(@PathVariable Integer id) {
        return purchaseService.getDetails(id);
    }

    /**
     * 作废订单
     */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Integer id, @RequestParam Integer employeeId) {
        return purchaseService.cancel(id, employeeId);
    }

    /**
     * 恢复订单
     */
    @PutMapping("/restore/{id}")
    public Result<Void> restore(@PathVariable Integer id, @RequestParam Integer employeeId) {
        return purchaseService.restore(id, employeeId);
    }

    /**
     * 管理员核实订单（DRAFT → HISTORY）
     */
    @PutMapping("/verify/{id}")
    public Result<Void> verify(@PathVariable Integer id, @RequestParam Integer adminId) {
        return purchaseService.verify(id, adminId);
    }
}
