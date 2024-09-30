package com.subrecommend.infra.in.web;

import com.subrecommend.infra.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = RecommendationController.class)
public class RecommendationControllerHelper {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), "RECOMMENDATION_ERROR");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}