package com.bwabwayo.app.global.s3.controller;

import com.bwabwayo.app.global.s3.dto.response.UploadFileDTO;
import com.bwabwayo.app.global.s3.dto.response.UploadResponseDTO;
import com.bwabwayo.app.global.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("api/upload")
public class UploadController {

    private final S3Service s3Service;
    
    @Operation(summary = "상품 이미지 업로드")
    @PostMapping("/product")
    public ResponseEntity<UploadResponseDTO> upload(@RequestPart List<MultipartFile> images) {
        List<UploadFileDTO> uploadImageDTOList = new ArrayList<>();

        try{
            for(MultipartFile image : images){
                // 파일이 이미지인지 검증
                String contentType = image.getContentType();

                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다. 파일명=" + image.getOriginalFilename() + ", MIME-type=" + contentType);
                }

                // 이미지 업로드
                UploadFileDTO uploadImageDTO = s3Service.uploadFile(image, "products");
                uploadImageDTOList.add(uploadImageDTO);
            }
        } catch (Exception e){
            log.error("이미지 업로드 실패: ", e);

            // 롤백: 업로드된 이미지 삭제
            for (UploadFileDTO uploadFileDTO : uploadImageDTOList) {
                try {
                    s3Service.deleteFile(uploadFileDTO.getKey());
                } catch (Exception ex) {
                    // 삭제 실패한 이미지는 로그로 남김
                    log.warn("롤백 중 이미지 삭제 실패: key={}", uploadFileDTO.getKey(), ex);
                }
            }
            throw e;
        }

        // Response DTO 생성
        UploadResponseDTO response = UploadResponseDTO.builder()
                .size(uploadImageDTOList.size())
                .result(uploadImageDTOList)
                .build();

        return ResponseEntity.ok(response);
    }
}
