package com.bwabwayo.app.global.s3.controller;

import com.bwabwayo.app.global.s3.dto.response.UploadResponseDTO;
import com.bwabwayo.app.global.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "파일 업로드")
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dir") String dirName) {
        UploadResponseDTO uploadResponseDTO = s3Service.uploadFile(file, dirName);
        return ResponseEntity.ok(uploadResponseDTO);
    }

    @Operation(summary = "파일 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam("key") String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Presigned URL 생성")
    @GetMapping("/presigned-url")
    public ResponseEntity<String> generatePresignedUrl(@RequestParam("key") String key) {
        String presignedUrl = s3Service.generatePresignedUrl(key, 1000 * 60 * 10);
        return ResponseEntity.ok(presignedUrl);
    }
}
