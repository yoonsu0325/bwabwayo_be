package com.bwabwayo.app.domain.product.exception;

import com.bwabwayo.app.global.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ProductExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(ProductNotFoundException e) {
        log.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of("404", e.getMessage()));
    }

    @ExceptionHandler(ProductUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleException(ProductUpdateNotAllowedException e) {
        log.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.of("403", e.getMessage()));
    }
}