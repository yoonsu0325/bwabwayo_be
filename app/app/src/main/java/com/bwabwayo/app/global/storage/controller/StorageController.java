package com.bwabwayo.app.global.storage.controller;

import com.bwabwayo.app.global.exception.dto.ErrorResponse;
import com.bwabwayo.app.global.storage.dto.response.UploadResponse;
import com.bwabwayo.app.global.storage.dto.response.UploadListResponse;
import com.bwabwayo.app.global.storage.exception.NotAllowedFileFormatException;
import com.bwabwayo.app.global.storage.service.StorageService;
import com.bwabwayo.app.global.storage.util.StorageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = { "/api/storage"})
public class StorageController {

    private final StorageService storageService;
    private final StorageUtil storageUtil;

    @Value("${storage.path.temp}")
    private String tempPath;


    @Operation(summary = "파일 업로드")
    @ApiResponse(responseCode = "200", description = "업로드 성공")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadListResponse> uploadFiles(
            @Valid @Size(min=1) @RequestParam  List<MultipartFile> files,
            @RequestParam String dir
    ) {
        List<UploadResponse> result = new ArrayList<>();

        try{
            for (MultipartFile file : files) {
                String key = storageService.upload(file, dir);
                String url = storageService.getUrlFromKey(key);

                UploadResponse upload = UploadResponse.from(key, url);

                result.add(upload);
            }
        } catch(Exception e){
            for (UploadResponse upload : result) {
                storageUtil.deleteWithoutException(upload.getKey());
            }
            throw e;
        }

        UploadListResponse response = UploadListResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadListResponse> uploadImages(@RequestParam List<MultipartFile> files) {
        ensureImages(files);
        return uploadFiles(files, tempPath);
    }


    @Operation(summary = "URL 기반 파일 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping("/upload/url")
    public ResponseEntity<UploadListResponse> uploadURL(
            @Valid @Size(min=1) @RequestParam List<String> urls,
            @RequestParam String dir){
        List<UploadResponse> result = new ArrayList<>();

        try{
            for (String url : urls) {
                String k = storageService.upload(url, dir);
                String u = storageService.getUrlFromKey(k);

                UploadResponse upload = UploadResponse.from(k, u);

                result.add(upload);
            }
        } catch(Exception e){
            for (UploadResponse upload : result) {
                storageUtil.deleteWithoutException(upload.getKey());
            }
            throw e;
        }
        return ResponseEntity.ok(UploadListResponse.from(result));
    }

    @Operation(summary = "파일 삭제")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam String key) {
        storageService.delete(key);
        return ResponseEntity.ok().build();
    }

    /* ============= 더미 API ================*/

    @Operation(summary = "상품의 이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadListResponse> uploadProductImages(@RequestParam List<MultipartFile> files) {
        ensureImages(files);
        return uploadImages(files);
    }

    @Operation(summary = "프로필 이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadListResponse> uploadProfileImages(@RequestParam List<MultipartFile> files) {
        ensureImages(files);
        return uploadImages(files);
    }

    @Operation(summary = "문의 이미지 업로드")
    @ApiResponse(responseCode = "200")
    @PostMapping(value = "/upload/inquiry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadListResponse> uploadInquiryImages(@RequestParam List<MultipartFile> files) {
        ensureImages(files);
        return uploadImages(files);
   }

    /* =========== 유틸리티 ======================== */

    /**
     * 이미지 파일인지 검증
     */
    private void ensureImages(List<MultipartFile> files){
        for (MultipartFile file : files) {
            ensureImage(file);
        }
    }

    private void ensureImage(MultipartFile file){
        String contentType = Optional.ofNullable(file.getContentType()).map(String::toLowerCase).orElse("");

        if (!contentType.startsWith("image/")) {
            throw new NotAllowedFileFormatException("이미지 파일만 업로드할 수 있습니다: file=" + file.getOriginalFilename() + " contentType=" + contentType);
        }
    }

    /* =========== 예외 처리 ======================== */

    @ExceptionHandler(NotAllowedFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleException(NotAllowedFileFormatException e) {
        log.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of("400", e.getMessage()));
    }
}
