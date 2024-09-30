package com.subrecommend.infra.in.web;

import com.subrecommend.infra.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = SubscriptionController.class)
public class SubscriptionControllerHelper {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), "SUBSCRIPTION_ERROR");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}