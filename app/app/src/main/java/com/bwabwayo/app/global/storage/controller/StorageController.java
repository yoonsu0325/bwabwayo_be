package com.bwabwayo.app.global.storage.controller;

import com.bwabwayo.app.domain.product.exception.BadRequestException;
import com.bwabwayo.app.global.storage.response.UploadFileDTO;
import com.bwabwayo.app.global.storage.response.UploadResponseDTO;
import com.bwabwayo.app.global.storage.service.S3Service;
import com.bwabwayo.app.global.storage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = { "/api/storage", "/api/s3"})
public class StorageController {

    private final StorageService storageService;
    private final S3Service s3Service;

    @Value("${storage.path.temp}")
    private String tempPath;
//    @Value("${storage.path.productImage}")
//    private String productPath;

    @Operation(summary = "상품의 이미지 업로드")
    @ApiResponse(responseCode = "200"
            , description = "업로드 성공"
            , content = @Content(mediaType = "application/json", schema = @Schema(implementation = UploadResponseDTO.class))
    )
    @PostMapping(value = "/upload/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProductImage(@RequestParam("files") List<MultipartFile> files) {
        List<UploadFileDTO> result = new ArrayList<>();
        
        // 파일 검사
        for (MultipartFile file : files) {
            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("이미지 파일만 업로드할 수 있습니다: file={}, contentType={}", file.getOriginalFilename(), contentType);
                throw new BadRequestException("이미지 파일만 업로드할 수 있습니다: file=" + file.getOriginalFilename() + " contentType=" + contentType);
            }
        }

        try {
            for (MultipartFile file : files) {
                String key = storageService.upload(file, tempPath);
                String url = storageService.getUrlFromKey(key);

                UploadFileDTO dto = UploadFileDTO.builder().key(key).url(url).build();

                result.add(dto);
            }
        } catch(Exception e){
            for (UploadFileDTO uploadFileDTO : result) {
                try{
                    s3Service.delete(uploadFileDTO.getKey());
                } catch(Exception ex){
                    log.warn("이미지 삭제 실패 key={}", uploadFileDTO.getKey());
                }
            }
            throw e;
        }

        UploadResponseDTO response = UploadResponseDTO.builder()
                .size(result.size())
                .results(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @Deprecated
    @Operation(summary = "파일 업로드")
    @ApiResponse(responseCode = "200",
            description = "업로드 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UploadResponseDTO.class))
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("dir") String dirName) {
        List<UploadFileDTO> result = new ArrayList<>();

        for (MultipartFile file : files) {
            String key = storageService.upload(file, dirName);
            String url = storageService.getUrlFromKey(key);

            UploadFileDTO dto = UploadFileDTO.builder()
                    .key(key)
                    .url(url)
                    .build();

            result.add(dto);
        }

        UploadResponseDTO response = UploadResponseDTO.builder()
                .size(result.size())
                .results(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @Deprecated
    @Operation(summary = "파일 삭제")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam("key") String fileName) {
        storageService.delete(fileName);
        return ResponseEntity.noContent().build();
    }

    @Deprecated
    @Operation(summary = "Presigned URL 생성")
    @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공")
    @GetMapping("/presigned-url")
    public ResponseEntity<String> generatePresignedUrl(@RequestParam("key") String key, @RequestParam @Min(0) Long expiration) {
        String presignedUrl = storageService.generatePresignedUrl(key, expiration * 1000L);
        return ResponseEntity.ok(presignedUrl);
    }
}
