package com.subrecommend.biz.exception;

public class BizException extends RuntimeException {
    private final String errorCode;

    public BizException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}