package com.bwabwayo.app.global.storage.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service implements StorageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public String upload(MultipartFile file, String dir) {
        String originalFilename = file.getOriginalFilename();
        String key = dir + "/" + UUID.randomUUID() + "_" + originalFilename;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata));

            return key;
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    @Override
    public void delete(String key) {
        amazonS3.deleteObject(bucketName, key);
    }

    @Override
    public void copy(String sourceKey, String targetKey) {
        if(!exists(sourceKey)){
            throw new IllegalArgumentException("Source key가 존재하지 않습니다: sourceKey=" + sourceKey);
        }
        amazonS3.copyObject(bucketName, sourceKey, bucketName, targetKey);
    }

    @Override
    public void move(String sourceKey, String targetKey) {
        if(!exists(sourceKey)){
            throw new IllegalArgumentException("Source key가 존재하지 않습니다: sourceKey=" + sourceKey);
        }
        amazonS3.copyObject(bucketName, sourceKey, bucketName, targetKey);
        amazonS3.deleteObject(bucketName, sourceKey);
    }

    @Override
    public String generatePresignedUrl(String key, long expiration) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expirationDate);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    @Override
    public boolean exists(String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    @Override
    public String getUrlFromKey(String key) {
        return amazonS3.getUrl(bucketName, key).toString();
    }
}
