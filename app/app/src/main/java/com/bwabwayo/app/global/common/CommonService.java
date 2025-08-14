package com.bwabwayo.app.global.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CommonService {
    public LocalDateTime parseSafe(String createdAt) {
        try {
            if (createdAt.endsWith("Z")) {
                return OffsetDateTime.parse(createdAt).toLocalDateTime();
            } else {
                return LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        } catch (Exception e) {
            log.warn("⚠️ 시간 파싱 실패: {}", createdAt);
            return LocalDateTime.MIN;
        }
    }
}
