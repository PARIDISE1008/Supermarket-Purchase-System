package com.supermarket.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.entity.PurchaseMain;
import com.supermarket.mapper.PurchaseMainMapper;

@Component
@EnableScheduling
public class PurchaseScheduler {

    private static final Logger log = LoggerFactory.getLogger(PurchaseScheduler.class);

    private final PurchaseMainMapper mainMapper;

    public PurchaseScheduler(PurchaseMainMapper mainMapper) {
        this.mainMapper = mainMapper;
    }

    /**
     * 每天17:00执行 1. DRAFT → HISTORY 2. 删除过期的 CANCELLED 订单 3. 模拟通知供应商
     */
    @Scheduled(cron = "0 0 17 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void deadlineTask() {
        log.info("[Deadline任务] 开始执行，时间={}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        // 1. 查询所有过期DRAFT订单
        List<PurchaseMain> expiredDrafts = mainMapper.selectExpiredDrafts(now);
        if (!expiredDrafts.isEmpty()) {
            List<Integer> ids = expiredDrafts.stream().map(PurchaseMain::getId).collect(Collectors.toList());
            mainMapper.batchUpdateToHistory(ids);

            log.info("[Deadline锁定] 共锁定{}笔订单", ids.size());
            for (PurchaseMain main : expiredDrafts) {
                log.info("[通知供应商] 采购单 {} 员工={} 总数量={} 总价={}",
                        main.getOrderNo(), main.getEmployeeId(), main.getTotalQuantity(), main.getTotalPrice());
            }
        } else {
            log.info("[Deadline锁定] 无过期DRAFT订单");
        }

        // 2. 物理删除过期的CANCELLED订单
        int deleted = mainMapper.deleteCancelledExpired(now);
        if (deleted > 0) {
            log.info("[Deadline清理] 共删除{}笔过期作废订单", deleted);
        }

        log.info("[Deadline任务] 执行完毕");
    }
}
