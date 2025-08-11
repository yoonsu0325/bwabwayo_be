package com.bwabwayo.app.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;

    private final String message;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

    private final Object details;


    public static ErrorResponse of(String code, String message) {
        return of(code, message, null);
    }

    public static ErrorResponse of(String code, String message, Object details) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}
