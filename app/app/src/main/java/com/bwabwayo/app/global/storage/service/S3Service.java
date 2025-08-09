package com.bwabwayo.app.global.storage.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service implements StorageService {
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 30000;

    private final AmazonS3 amazonS3;
    private final TransferManager transferManager;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public String upload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        
        // 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || extension.isBlank()) extension = "bin";

        // 키명 생성
        String key = dir + "/" + getPrefix() + "." + extension;

        // contentType
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        // 메타데이터
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(contentType);

        log.info("S3 업로드 시작: file={}, size={} bytes, key={}", originalFilename, file.getSize(), key);

        try (InputStream in = file.getInputStream()){
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, in, metadata);

            Upload upload = transferManager.upload(putRequest);

            upload.addProgressListener((com.amazonaws.event.ProgressListener) e -> {
                switch (e.getEventType()) {
                    case TRANSFER_STARTED_EVENT:
                        log.info("업로드 시작됨: key={}", key); break;
                    case REQUEST_BYTE_TRANSFER_EVENT:
                        double pct = upload.getProgress().getPercentTransferred();
                        log.debug("업로드 진행률: {}%", String.format("%.2f", pct));
                        break;
                    case TRANSFER_COMPLETED_EVENT:
                        log.info("업로드 완료: key={}", key); break;
                    case TRANSFER_FAILED_EVENT:
                        log.error("업로드 실패: key={}", key); break;
                }
            });

            upload.waitForCompletion(); // 완료 대기
        } catch (Exception e) {
            log.error("S3 업로드 실패: file={}, key={}, cause={}", originalFilename, key, e.getMessage(), e);
            throw new RuntimeException("파일 업로드 실패: file=" + originalFilename + ", key=" + key, e);
        }
        return key;
    }

    @Override
    public String upload(String srcURL, String dir) {
        HttpURLConnection connection = null;
        String key = null;
        try {
            URL url = new URL(srcURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);

            // content-type
            String contentType = connection.getContentType();
            if(contentType == null || contentType.isBlank()) contentType = "application/octet-stream";

            // content-length
            long contentLength = connection.getContentLengthLong();

            // 확장자
            String extension = StringUtils.getFilenameExtension(url.getPath());
            if(extension == null || extension.isBlank()) extension = contentType.substring(contentType.lastIndexOf("/") + 1);
            if(extension.isBlank()) extension = "bin";

            key = dir + "/" + getPrefix() + "." + extension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            if(contentLength > 0) metadata.setContentLength(contentLength);

            try (InputStream inputStream = connection.getInputStream()) {
                PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
                Upload upload = transferManager.upload(putRequest);
                upload.waitForCompletion();
            }
        } catch (Exception e) {
            log.error("S3 업로드 실패: url={}, key={}", srcURL, key, e);
            throw new RuntimeException("S3 업로드 실패: url=" + srcURL + ", key=" + key, e);
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return key;
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
    public boolean exists(String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    @Override
    public String getUrlFromKey(String key) {
        return amazonS3.getUrl(bucketName, key).toString();
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

    private static String getPrefix(){
        String uuid = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return uuid + "_" + timestamp;
    }
}
