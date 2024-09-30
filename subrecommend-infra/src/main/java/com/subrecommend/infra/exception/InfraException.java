package com.subrecommend.infra.exception;

public class InfraException extends RuntimeException {
    private final String errorCode;

    public InfraException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}