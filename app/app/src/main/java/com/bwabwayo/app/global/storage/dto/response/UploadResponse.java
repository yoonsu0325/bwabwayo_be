package com.bwabwayo.app.global.storage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {
    private String key;
    private String url;

    public static UploadResponse from(String key, String url) {
        return UploadResponse.builder()
                .key(key)
                .url(url)
                .build();
    }
}
