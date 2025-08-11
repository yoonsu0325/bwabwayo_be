package com.bwabwayo.app.global.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * 파일을 저장하고, 저장된 파일의 고유 key를 반환합니다.
     *
     * @param file 저장할 파일
     * @param dir 저장할 디렉터리(버킷 내 폴더 경로처럼 사용)
     * @return 저장된 파일의 key
     */
    String upload(MultipartFile file, String dir);

    String upload(String url, String dir);

    /**
     * 파일을 삭제합니다.
     *
     * @param key 삭제할 파일의 고유 key
     */
    void delete(String key);

    /**
     * 지정된 sourceKey의 파일을 targetKey 위치로 복사합니다.
     *
     * @param sourceKey 이동할 원본 객체의 키
     * @param targetKey 복사 대상 객체의 키
     */
    void copy(String sourceKey, String targetKey);

    /**
     * 지정된 sourceKey의 파일을 targetKey 위치로 이동합니다.
     * (복사 후 원본 삭제 방식)
     *
     * @param sourceKey 이동할 원본 객체의 키
     * @param targetKey 이동 대상 객체의 키
     */
    default void move(String sourceKey, String targetKey){
        copy(sourceKey, targetKey);
        delete(sourceKey);
    }


    /**
     * 지정된 키에 대한 Presigned URL을 생성합니다.
     *
     * @param key        저장된 파일의 고유 식별자 (예: 폴더/파일명.jpg)
     * @param expiration Presigned URL의 유효 시간(초 단위 또는 밀리초 단위)
     * @return 유효 기간 내 접근 가능한 서명된 URL 문자열
     */
    String generatePresignedUrl(String key, long expiration);

    /**
     * 파일 존재 여부 확인
     *
     * @param key 파일의 key
     * @return 존재하면 true
     */
    boolean exists(String key);

    /**
     * 주어진 key로 접근 가능한 정적 또는 presigned URL을 반환합니다.
     */
    default String getUrlFromKey(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 주어진 URL로부터 key를 추출합니다.
     */
    default String getKeyFromUrl(String url){
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

