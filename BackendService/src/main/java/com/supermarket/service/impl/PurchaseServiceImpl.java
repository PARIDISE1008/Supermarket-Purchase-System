package com.supermarket.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.common.Result;
import com.supermarket.dto.PurchaseSubmitDTO;
import com.supermarket.entity.Goods;
import com.supermarket.entity.PurchaseDetail;
import com.supermarket.entity.PurchaseMain;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.GoodsMapper;
import com.supermarket.mapper.PurchaseDetailMapper;
import com.supermarket.mapper.PurchaseMainMapper;
import com.supermarket.service.PurchaseService;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseServiceImpl.class);

    private final PurchaseMainMapper mainMapper;
    private final PurchaseDetailMapper detailMapper;
    private final GoodsMapper goodsMapper;

    // 第2层：30秒限流（employeeId → 上次提交时间）
    private final Map<Integer, Long> lastSubmitTime = new ConcurrentHashMap<>();

    // 第3层：订单号防重复（orderNo → 是否已使用，30分钟过期）
    private final Map<String, Boolean> usedOrderNos = new ConcurrentHashMap<>();

    public PurchaseServiceImpl(PurchaseMainMapper mainMapper,
            PurchaseDetailMapper detailMapper,
            GoodsMapper goodsMapper) {
        this.mainMapper = mainMapper;
        this.detailMapper = detailMapper;
        this.goodsMapper = goodsMapper;
    }

    @Override
    public Result<String> generateOrderNo() {
        String orderNo = "PRE-" + System.currentTimeMillis();
        usedOrderNos.put(orderNo, false);
        log.debug("[预生成订单号] orderNo={}", orderNo);
        return Result.success(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> submit(PurchaseSubmitDTO dto) {
        // ========== Fail-fast 校验 ==========
        if (dto.getEmployeeId() == null) {
            log.warn("[提交采购失败] 员工ID为空");
            throw BusinessException.paramError("员工ID不能为空");
        }
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            log.warn("[提交采购失败] 采购明细为空 employeeId={}", dto.getEmployeeId());
            throw BusinessException.paramError("采购明细不能为空");
        }
        if (dto.getPreOrderNo() == null || dto.getPreOrderNo().isEmpty()) {
            log.warn("[提交采购失败] 预订单号为空 employeeId={}", dto.getEmployeeId());
            throw BusinessException.paramError("预订单号不能为空");
        }

        // ========== 第2层：30秒限流 ==========
        Long lastTime = lastSubmitTime.get(dto.getEmployeeId());
        if (lastTime != null && System.currentTimeMillis() - lastTime < 30000) {
            long remain = 30 - (System.currentTimeMillis() - lastTime) / 1000;
            log.warn("[限流拦截] employeeId={}, 还需等待{}秒", dto.getEmployeeId(), remain);
            throw new BusinessException(429, "操作太频繁，请" + remain + "秒后重试");
        }

        // ========== 第3层：订单号防重复 ==========
        Boolean used = usedOrderNos.get(dto.getPreOrderNo());
        if (used == null || used) {
            log.warn("[订单号重复] orderNo={}", dto.getPreOrderNo());
            throw new BusinessException("订单号已使用或无效，请刷新页面重新提交");
        }
        usedOrderNos.put(dto.getPreOrderNo(), true);

        // ========== 计算总价（不信任前端） ==========
        List<PurchaseDetail> details = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (PurchaseSubmitDTO.DetailItem item : dto.getDetails()) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods == null) {
                log.warn("[提交采购失败] 商品不存在 goodsId={}", item.getGoodsId());
                throw BusinessException.notFound("商品ID：" + item.getGoodsId());
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                log.warn("[提交采购失败] 数量非法 goodsId={}, quantity={}", item.getGoodsId(), item.getQuantity());
                throw BusinessException.paramError("采购数量必须大于0");
            }

            BigDecimal unitPrice = goods.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(item.getQuantity()));

            PurchaseDetail detail = new PurchaseDetail();
            detail.setGoodsId(item.getGoodsId());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(unitPrice);
            detail.setTotalPrice(itemTotal);
            details.add(detail);

            totalPrice = totalPrice.add(itemTotal);
            totalQuantity += item.getQuantity();
        }

        // ========== 解析采购时间 ==========
        LocalDateTime purchaseTime = LocalDateTime.now();
        if (dto.getPurchaseTime() != null && !dto.getPurchaseTime().isEmpty()) {
            try {
                purchaseTime = LocalDateTime.parse(dto.getPurchaseTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                log.warn("[提交采购失败] 时间格式错误 purchaseTime={}", dto.getPurchaseTime());
                throw BusinessException.paramError("采购时间格式不正确");
            }
        }

        // ========== 检查今天是否已有 DRAFT 订单 ==========
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        PurchaseMain existingDraft = mainMapper.selectTodayDraft(dto.getEmployeeId(), todayStart);

        String orderNo;
        if (existingDraft != null) {
            // 已有草稿 → UPDATE
            log.info("[采购更新] 员工已有今日草稿，更新明细 employeeId={}, draftId={}", dto.getEmployeeId(), existingDraft.getId());

            existingDraft.setTotalQuantity(totalQuantity);
            existingDraft.setTotalPrice(totalPrice);
            existingDraft.setRemark(dto.getRemark());
            mainMapper.update(existingDraft);

            // 删除旧明细，插入新明细
            detailMapper.deleteByMainId(existingDraft.getId());
            for (PurchaseDetail detail : details) {
                detail.setPurchaseMainId(existingDraft.getId());
            }
            detailMapper.batchInsert(details);

            orderNo = existingDraft.getOrderNo();
            log.info("[采购更新成功] orderNo={}, totalQuantity={}, totalPrice={}", orderNo, totalQuantity, totalPrice);

        } else {
            // 无草稿 → INSERT
            orderNo = generateFinalOrderNo();
            log.info("[采购新建] 创建新草稿 employeeId={}, orderNo={}", dto.getEmployeeId(), orderNo);

            // 计算截止时间（今天17:00）
            LocalDateTime deadline = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0));

            PurchaseMain main = new PurchaseMain();
            main.setOrderNo(orderNo);
            main.setEmployeeId(dto.getEmployeeId());
            main.setTotalQuantity(totalQuantity);
            main.setTotalPrice(totalPrice);
            main.setPurchaseTime(purchaseTime);
            main.setStatus("DRAFT");
            main.setDeadlineTime(deadline);
            main.setRemark(dto.getRemark());
            mainMapper.insert(main);

            for (PurchaseDetail detail : details) {
                detail.setPurchaseMainId(main.getId());
            }
            detailMapper.batchInsert(details);

            log.info("[采购新建成功] orderNo={}, totalQuantity={}, totalPrice={}", orderNo, totalQuantity, totalPrice);
        }

        // ========== 记录限流时间 ==========
        lastSubmitTime.put(dto.getEmployeeId(), System.currentTimeMillis());

        return Result.success("采购单已生成", orderNo);
    }

    @Override
    public Result<List<PurchaseMain>> listMyOrders(Integer employeeId, String status, Integer page, Integer size) {
        if (employeeId == null) {
            throw BusinessException.paramError("员工ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        int offset = (page - 1) * size;
        List<PurchaseMain> list = mainMapper.selectPage(employeeId, status, offset, size);
        int total = mainMapper.count(employeeId, status);

        log.debug("[查询采购订单] employeeId={}, status={}, page={}, total={}", employeeId, status, page, total);
        return Result.success("查询成功", list, total);
    }

    @Override
    public Result<PurchaseMain> getDetail(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            throw BusinessException.paramError("订单ID不合法");
        }

        PurchaseMain main = mainMapper.selectById(orderId);
        if (main == null) {
            throw BusinessException.notFound("采购单");
        }

        List<PurchaseDetail> details = detailMapper.selectByMainId(orderId);
        // 这里需要在 PurchaseMain 加一个 details 字段，课设简化：直接返回 main，前端再单独调接口查明细
        // 或者直接在 Result 里多返回一个字段

        log.debug("[查询采购详情] orderId={}, orderNo={}", orderId, main.getOrderNo());
        return Result.success(main);
    }

    /**
     * 查询订单的明细列表（单独接口）
     */
    public Result<List<PurchaseDetail>> getDetails(Integer orderId) {
        List<PurchaseDetail> details = detailMapper.selectByMainId(orderId);
        return Result.success(details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancel(Integer orderId, Integer employeeId) {
        PurchaseMain main = mainMapper.selectById(orderId);
        if (main == null) {
            throw BusinessException.notFound("采购单");
        }
        if (!main.getEmployeeId().equals(employeeId)) {
            log.warn("[作废订单失败] 无权操作 employeeId={}, orderEmployeeId={}", employeeId, main.getEmployeeId());
            throw new BusinessException("只能操作自己的订单");
        }
        if (!main.isDraft()) {
            log.warn("[作废订单失败] 状态不允许 orderId={}, status={}", orderId, main.getStatus());
            throw new BusinessException("只有草稿状态的订单可以作废");
        }

        int rows = mainMapper.cancel(orderId);
        if (rows == 0) {
            throw BusinessException.operationFailed("作废失败");
        }

        log.info("[作废订单成功] orderId={}, orderNo={}", orderId, main.getOrderNo());
        return Result.success("订单已作废", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> restore(Integer orderId, Integer employeeId) {
        PurchaseMain main = mainMapper.selectById(orderId);
        if (main == null) {
            throw BusinessException.notFound("采购单");
        }
        if (!main.getEmployeeId().equals(employeeId)) {
            log.warn("[恢复订单失败] 无权操作 employeeId={}, orderEmployeeId={}", employeeId, main.getEmployeeId());
            throw new BusinessException("只能操作自己的订单");
        }
        if (!main.isCancelled()) {
            log.warn("[恢复订单失败] 状态不允许 orderId={}, status={}", orderId, main.getStatus());
            throw new BusinessException("只有已作废的订单可以恢复");
        }
        if (main.getDeadlineTime() != null && main.getDeadlineTime().isBefore(LocalDateTime.now())) {
            log.warn("[恢复订单失败] 已过截止时间 orderId={}, deadline={}", orderId, main.getDeadlineTime());
            throw new BusinessException("已过截止时间，无法恢复");
        }

        int rows = mainMapper.restore(orderId);
        if (rows == 0) {
            throw BusinessException.operationFailed("恢复失败");
        }

        log.info("[恢复订单成功] orderId={}, orderNo={}", orderId, main.getOrderNo());
        return Result.success("订单已恢复", null);
    }

    /**
     * 生成最终采购单号
     */
    private String generateFinalOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long suffix = System.currentTimeMillis() % 100000;
        return String.format("PO-%s-%05d", dateStr, suffix);
    }
}
