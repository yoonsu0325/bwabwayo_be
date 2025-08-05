package com.bwabwayo.app.global.storage.util;

import com.bwabwayo.app.domain.product.exception.NotFoundException;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageUtil {
    private final StorageService storageService;

    @Value("${storage.path.temp}")
    private String tempPath;

    public void safeDelete(String key) {
        try {
            storageService.delete(key);
        } catch (Exception e) {
            // 삭제 실패한 이미지는 로그로 남김
            log.info("삭제 실패: key={}", key, e);
        }
    }

    public List<String> copyToPermanentDirectory(List<String> srcKeys, String targetDir) {
        List<String> result = new ArrayList<>();

        for (String srcKey : srcKeys) {
            String targetKey = copyToPermanentDirectory(srcKey, targetDir);
            result.add(targetKey);
        }
        return result;
    }

    public String copyToPermanentDirectory(String srcKey, String targetDir) {
        if (!storageService.exists(srcKey)) {
            throw new NotFoundException("스토리지에 존재하지 않는 이미지 입니다. key=" + srcKey);
        }

        String targetKey = null;
        if (srcKey.startsWith(tempPath)) {
            targetKey = targetDir + srcKey.substring(tempPath.length());
            storageService.copy(srcKey, targetKey);
        }
        return targetKey;
    }

    public void rollbackTemporalImages(List<String> imageKeys, String targetDir){
        for(String imageKey : imageKeys){
            rollbackTemporalImages(imageKey, targetDir);
        }
    }

    public void rollbackTemporalImages(String imageKey, String targetDir){
        if(imageKey.startsWith(tempPath)){
            String deletingKey = targetDir + imageKey.substring(tempPath.length());
            safeDelete(deletingKey);
        }
    }
}
