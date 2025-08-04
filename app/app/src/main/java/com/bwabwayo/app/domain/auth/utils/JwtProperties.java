package com.bwabwayo.app.domain.auth.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long accessExpMinutes;
    private long refreshExpMinutes;
    private String header;
    private String type;
    private String typeRefresh;
    private String typeAccess;
    private String aesSecretKey;
    private String aesIv;
    private long refreshReissueThresholdDays;
}
