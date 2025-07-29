package com.bwabwayo.app.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    /**
     * 파일 업로드
     * @param file 업로드할 파일
     * @param dirName 업로드할 경로
     * @return 업로드된 파일의 URL
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dir") String dirName) {
        String url = s3Service.uploadFile(file, dirName);
        return ResponseEntity.ok(url);
    }

    /**
     * 파일 삭제
     * @param fileName 삭제할 파일의 경로
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam("key") String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Presigned URL 생성
     * @param key Presigned URL을 생성할 대상
     * @return Presigned URL
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<String> generatePresignedUrl(@RequestParam("key") String key) {
        String presignedUrl = s3Service.generatePresignedUrl(key, 1000 * 60 * 10);
        return ResponseEntity.ok(presignedUrl);
    }
}
