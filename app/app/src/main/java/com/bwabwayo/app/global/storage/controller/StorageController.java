package com.bwabwayo.app.global.storage.controller;

import com.bwabwayo.app.global.exception.dto.ErrorResponse;
import com.bwabwayo.app.global.storage.dto.response.UploadFileResponse;
import com.bwabwayo.app.global.storage.dto.response.UploadResponse;
import com.bwabwayo.app.global.storage.exception.NotAllowedFileFormatException;
import com.bwabwayo.app.global.storage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = { "/api/storage"})
public class StorageController {

    private final StorageService storageService;

    @Value("${storage.path.temp}")
    private String tempPath;


    @Operation(summary = "파일 업로드")
    @ApiResponse(responseCode = "200", description = "업로드 성공")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("dir") String dirName) {
        List<UploadFileResponse> result = new ArrayList<>();

        try{
            for (MultipartFile file : files) {
                String key = storageService.upload(file, dirName);
                String url = storageService.getUrlFromKey(key);

                UploadFileResponse dto = UploadFileResponse.builder().key(key).url(url).build();

                result.add(dto);
            }
        } catch(Exception e){
            for (UploadFileResponse uploadFileDTO : result) {
                try{
                    storageService.delete(uploadFileDTO.getKey());
                } catch(Exception ex){
                    log.warn("이미지 삭제 실패 key={}", uploadFileDTO.getKey());
                }
            }
            throw e;
        }

        UploadResponse response = UploadResponse.builder()
                .size(result.size())
                .results(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        ensureImages(files);
        return uploadFiles(files, tempPath);
    }

    @Operation(summary = "상품의 이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadProductImages(@RequestParam("files") List<MultipartFile> files) {
        ensureImages(files);
        return uploadImages(files);
    }

    @Operation(summary = "프로필 이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadProfileImages(@RequestParam("files") List<MultipartFile> files) {
        ensureImages(files);
        return uploadImages(files);
    }

    @Operation(summary = "문의 이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/inquiry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadInquiryImages(@RequestParam("files") List<MultipartFile> files) {
        ensureImages(files);
        return uploadImages(files);
   }

    @PostMapping("/upload/url")
    public ResponseEntity<?> uploadURL(@RequestParam String url, @RequestParam String dir){
        String key = storageService.upload(url, dir);
        return ResponseEntity.ok(UploadFileResponse.builder()
                .key(key)
                .url(storageService.getUrlFromKey(key))
                .build()
        );
    }

    @Deprecated
    @Operation(summary = "파일 삭제")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam String key) {
        storageService.delete(key);
        return ResponseEntity.ok().build();
    }


    /* =========== 유틸리티 ======================== */

    /**
     * 이미지 파일인지 검증
     */
    private void ensureImages(List<MultipartFile> files){
        for (MultipartFile file : files) {
            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new NotAllowedFileFormatException("이미지 파일만 업로드할 수 있습니다: file=" + file.getOriginalFilename() + " contentType=" + contentType);
            }
        }
    }

    /* =========== 예외 처리 ======================== */

    @ExceptionHandler(NotAllowedFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleException(NotAllowedFileFormatException e) {
        log.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of("400", e.getMessage()));
    }
}
