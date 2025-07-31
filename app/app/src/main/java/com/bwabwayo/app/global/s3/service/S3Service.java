package com.bwabwayo.app.global.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bwabwayo.app.global.s3.dto.response.UploadResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    /**
     * S3에 파일을 업로드
     */
    public UploadResponseDTO uploadFile(MultipartFile file, String path) {
        // 이중 슬래시 방지
        if (path != null && path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        // 파일명이 비어있다면 임의 부여
        String originalFileName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
        // 파일명 URL 인코딩
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
        // key 생성
        String fileName = path + "/" + UUID.randomUUID() + "_" + encodedFileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

            URL url = amazonS3.getUrl(bucketName, fileName);
            String key = fileName;
            if(key.startsWith("/")) key = key.substring(1);

            return UploadResponseDTO.builder()
                    .key(key)
                    .url(url.toString())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }


    /**
     * S3에서 지정한 파일을 삭제
     * @param key 삭제할 파일의 객체 키
     */
    public void deleteFile(String key) {
        amazonS3.deleteObject(bucketName, key);
    }


    /**
     * S3 객체의 경로(키)를 변경 (복사 후 원본 삭제)
     * @param sourceKey 기존 파일 경로
     * @param targetKey 새 파일 경로
     */
    public void moveFile(String sourceKey, String targetKey) {
        if (!amazonS3.doesObjectExist(bucketName, sourceKey)) {
            throw new RuntimeException("원본 파일이 존재하지 않습니다: " + sourceKey);
        }

        amazonS3.copyObject(bucketName, sourceKey, bucketName, targetKey);

        amazonS3.deleteObject(bucketName, sourceKey);
    }

    /**
     * S3에 파일이 존재하는지 확인
     * @param key 확인할 파일의 key
     * @return 존재하면 true, 아니면 false
     */
    public boolean exists(String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }


    /**
     * Presigned URL 생성
     * @param key Presigned URL을 생성할 대상 파일의 url
     * @return Presigned URL
     */
    public String generatePresignedUrl(String key, long timeout) {
        Date expiration = new Date(System.currentTimeMillis() + timeout); // 10분

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    /**
     * S3 객체 키를 공개 URL로 변환
     *
     * @param key S3에 저장된 파일의 객체 키
     * @return 접근 가능한 공개 URL
     */
    public String getUrl(String key){
        return amazonS3.getUrl(bucketName, key).toString();
    }
}