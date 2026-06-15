package com.supermarket.exception;

public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    public Integer getCode() { return code; }

    @Override
    public String getMessage() { return message; }

    public static BusinessException paramError(String message) {
        return new BusinessException(400, "参数错误：" + message);
    }

    public static BusinessException notFound(String resourceName) {
        return new BusinessException(404, resourceName + "不存在");
    }

    public static BusinessException duplicate(String fieldName) {
        return new BusinessException(409, fieldName + "已存在");
    }

    public static BusinessException operationFailed(String message) {
        return new BusinessException(500, message);
    }
}