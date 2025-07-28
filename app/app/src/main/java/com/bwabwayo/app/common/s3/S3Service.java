package com.bwabwayo.app.common.s3;

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
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 파일 업로드
     * @param file s3에 업로드할 파일 객체
     * @param dirName 파일을 저장할 경로
     * @return 파일 업로드에 성공하였다면 저장된 위치(url)를 반환
     */
    public String uploadFile(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

            return amazonS3.getUrl(bucketName, fileName).toString(); // URL 반환
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    /**
     * 파일 삭제
     * @param fileName 삭제할 파일의 경로를 포함한 이름
     */
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(bucketName, fileName);
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
}