package com.supermarket.exception;

import com.supermarket.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@Valid 失败）
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("[参数校验失败] {}", message);
        return Result.paramError(message);
    }

    /**
     * 事务异常（回滚时触发）
     */
    @ExceptionHandler(TransactionSystemException.class)
    public Result<?> handleTransactionException(TransactionSystemException e) {
        log.error("[事务回滚] 事务执行失败，已回滚", e);
        return Result.error(500, "操作失败，数据已回滚，请重试");
    }

    /**
     * 兜底异常（未知错误）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("[系统异常] 未知错误", e);
        return Result.systemError();
    }
}
