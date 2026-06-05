// ============================================
// 路径: com/supermarket/dto/PurchaseSubmitDTO.java
// ============================================
package com.supermarket.dto;

import java.util.List;

/**
 * 前端提交采购单的数据结构
 */
public class PurchaseSubmitDTO {

    /**
     * 预生成订单号（用于防重复）
     */
    private String preOrderNo;

    /**
     * 采购员工ID
     */
    private Integer employeeId;

    /**
     * 采购明细列表
     */
    private List<DetailItem> details;

    /**
     * 采购时间（可选，默认当前时间）
     */
    private String purchaseTime;

    /**
     * 备注
     */
    private String remark;

    // 内部类：一条明细
    public static class DetailItem {

        private Integer goodsId;
        private Integer quantity;

        public Integer getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(Integer goodsId) {
            this.goodsId = goodsId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    // ==================== Getter/Setter ====================
    public String getPreOrderNo() {
        return preOrderNo;
    }

    public void setPreOrderNo(String preOrderNo) {
        this.preOrderNo = preOrderNo;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public List<DetailItem> getDetails() {
        return details;
    }

    public void setDetails(List<DetailItem> details) {
        this.details = details;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
