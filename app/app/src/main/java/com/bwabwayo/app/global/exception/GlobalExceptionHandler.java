package com.bwabwayo.app.global.exception;

import com.bwabwayo.app.domain.product.controller.CategoryController;
import com.bwabwayo.app.global.s3.controller.UploadController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        CategoryController.class,
        UploadController.class
})
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", e.getMessage() == null || e.getMessage().isEmpty() ? "잘못된 요청입니다." : e.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.badRequest().body(body);
    }
}
