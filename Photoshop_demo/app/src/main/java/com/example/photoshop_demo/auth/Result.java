package com.example.photoshop_demo.auth;

/**
 * 通用返回结果类
 * 用于封装操作结果和错误信息
 */
public class Result<T> {
    private boolean success;
    private T data;
    private String errorMessage;
    private ErrorCode errorCode;

    // 错误码枚举
    public enum ErrorCode {
        SUCCESS(0, "操作成功"),
        USER_EXISTS(1001, "用户名已存在"),
        USER_NOT_FOUND(1002, "用户不存在"),
        INVALID_PASSWORD(1003, "密码错误"),
        INVALID_USERNAME(1004, "用户名格式不正确"),
        INVALID_EMAIL(1005, "邮箱格式不正确"),
        WEAK_PASSWORD(1006, "密码强度不足"),
        DATABASE_ERROR(2001, "数据库错误"),
        UNKNOWN_ERROR(9999, "未知错误");

        private final int code;
        private final String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    // 私有构造函数
    private Result(boolean success, T data, String errorMessage, ErrorCode errorCode) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    // 成功返回
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, ErrorCode.SUCCESS);
    }

    // 失败返回
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(false, null, errorCode.getMessage(), errorCode);
    }

    // 失败返回（自定义消息）
    public static <T> Result<T> error(ErrorCode errorCode, String customMessage) {
        return new Result<>(false, null, customMessage, errorCode);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

