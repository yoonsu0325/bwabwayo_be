package com.bwabwayo.app.domain.product.event;

import com.bwabwayo.app.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventHandler {

    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        for (String key : event.imageKeys()) {
            try {
                s3Service.deleteFile(key);
            } catch (Exception e) {
                // 삭제 실패한 이미지를 로깅 또는 실패 목록 저장
                log.warn("S3 삭제 실패: {}", key, e);
            }
        }
    }
}
