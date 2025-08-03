package com.bwabwayo.app.global.storage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponseDTO {
    private int size; // 업로드한 파일의 수
    @Builder.Default
    private List<UploadFileDTO> results = new ArrayList<>(); // 업로드된 파일의 정보
}
