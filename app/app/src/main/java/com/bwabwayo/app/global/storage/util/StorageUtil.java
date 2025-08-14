package com.bwabwayo.app.global.storage.util;

import com.bwabwayo.app.global.exception.NotFoundException;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageUtil {
    private final boolean ENABLE_REALTIME_DELETE = false;

    private final StorageService storageService;

    public void deleteWithoutException(String key) {
        if(!ENABLE_REALTIME_DELETE) return;

        try {
            storageService.delete(key);
        } catch (Exception e) {
            // 삭제 실패한 이미지는 로그로 남김
            log.info("이미지 삭제 실패: key={}", key, e);
        }
    }

    /**
     * keys 중 prefix가 srcPrefix인 key를 tgtPrefix로 복사
     */
    public List<String> copyToDirectory(List<String> keys, String srcPrefix, String tgtPrefix) {
        List<String> result = new ArrayList<>();

        for (String srcKey : keys) {
            String tgtKey = copyToDirectory(srcKey, srcPrefix, tgtPrefix);
            result.add(tgtKey);
        }
        return result;
    }

    public String copyToDirectory(String key, String srcPrefix, String tgtPrefix) {
        if (!storageService.exists(key)) {
            log.info("스토리지에 존재하지 않는 파일: key={}", key);
            return key;
        }

        String tgtKey = key;
        if (key.startsWith(srcPrefix)) {
            tgtKey = tgtPrefix + key.substring(srcPrefix.length());
            storageService.copy(key, tgtKey);
        }
        return tgtKey;
    }

    /**
     * keys 중 prefix가 srcPrefix가 tgtPrefix로 복사된 key를 삭제
     */
    public void rollback(List<String> keys, String srcPrefix, String tgtPrefix){
        if(!ENABLE_REALTIME_DELETE) return;

        for(String key : keys){
            rollback(key, srcPrefix, tgtPrefix);
        }
    }

    public void rollback(String key, String srcPrefix, String tgtPrefix){
        if(!ENABLE_REALTIME_DELETE) return;

        if(key.startsWith(srcPrefix)){
            String deletingKey = tgtPrefix + key.substring(srcPrefix.length());
            deleteWithoutException(deletingKey);
        }
    }
}
