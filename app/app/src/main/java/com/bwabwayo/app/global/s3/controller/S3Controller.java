package com.bwabwayo.app.global.s3.controller;

import com.bwabwayo.app.global.s3.dto.response.UploadFileDTO;
import com.bwabwayo.app.global.s3.dto.response.UploadResponseDTO;
import com.bwabwayo.app.global.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "파일 업로드")
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("dir") String dirName) {

        List<UploadFileDTO> fileDTOList = new ArrayList<>();

        for (MultipartFile file : files) {
            UploadFileDTO fileDTO = s3Service.uploadFile(file, dirName);
            fileDTOList.add(fileDTO);
        }

        UploadResponseDTO response = UploadResponseDTO.builder()
                .size(fileDTOList.size())
                .result(fileDTOList)
                .build();

        return ResponseEntity.ok(response);
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
