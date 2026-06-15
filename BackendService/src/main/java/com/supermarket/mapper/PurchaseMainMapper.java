package com.supermarket.mapper;

import com.supermarket.entity.PurchaseMain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PurchaseMainMapper {

    int insert(PurchaseMain main);

    PurchaseMain selectById(@Param("id") Integer id);

    PurchaseMain selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询某个员工今天的 DRAFT 订单（一天一单）
     */
    PurchaseMain selectTodayDraft(@Param("employeeId") Integer employeeId,
            @Param("today") LocalDateTime today);

    /**
     * 分页查询（按员工+状态筛选）
     */
    List<PurchaseMain> selectPage(@Param("employeeId") Integer employeeId,
            @Param("status") String status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int count(@Param("employeeId") Integer employeeId,
            @Param("status") String status);

    int update(PurchaseMain main);

    /**
     * 作废：DRAFT → CANCELLED
     */
    int cancel(@Param("id") Integer id);

    /**
     * 恢复：CANCELLED → DRAFT
     */
    int restore(@Param("id") Integer id);

    /**
     * 查询所有 DRAFT 且已过截止时间的订单
     */
    List<PurchaseMain> selectExpiredDrafts(@Param("now") LocalDateTime now);

    /**
     * 批量更新状态为 HISTORY
     */
    int batchUpdateToHistory(@Param("ids") List<Integer> ids);

    /**
     * 物理删除已作废且过期的订单
     */
    int deleteCancelledExpired(@Param("now") LocalDateTime now);

    /**
     * 管理员核实：DRAFT → HISTORY
     */
    int verify(@Param("id") Integer id);
}
