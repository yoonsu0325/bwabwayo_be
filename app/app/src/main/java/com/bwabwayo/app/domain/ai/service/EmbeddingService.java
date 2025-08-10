package com.bwabwayo.app.domain.ai.service;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.QueryItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmbeddingService {

    @Value("${qdrant.base-url}")
    private String qdrantUrl;

    @Value("${qdrant.collection-name}")
    private String collectionName;

    @Value("${qdrant.vector-size}")
    private int vectorSize;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    /* ===================== Upsert ===================== */

    /** Qdrant에 Vector를 Upsert */
    public void upsertPoint(QdrantPointDto pointDto) {
        upsertPoints(List.of(pointDto));
    }

    public void upsertPoints(List<QdrantPointDto> pointDtos) {
        final String url = qdrantUrl + "/collections/" + collectionName + "/points?wait=true";

        List<Map<String, Object>> points = new ArrayList<>();
        for (QdrantPointDto pointDto : pointDtos) {
            // 1. vectors 구성
            ensureSize("title", pointDto.getTitleVector());
            ensureSize("category", pointDto.getCategoryVector());

            Map<String, Object> vectors = new HashMap<>();
            vectors.put("title", pointDto.getTitleVector());
            vectors.put("category", pointDto.getCategoryVector());

            // 2. payload 구성
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", pointDto.getTitle());
            payload.put("category", pointDto.getCategoryName());
            payload.put("categoryId", pointDto.getCategoryId());
            payload.put("price", pointDto.getPrice());
            payload.put("isSale", pointDto.getIsSale());

            // 3. point 구성
            Map<String, Object> point = new HashMap<>();
            point.put("id", pointDto.getId());
            point.put("vector", vectors);
            point.put("payload", payload);

            points.add(point);
        }

        // request body 생성
        Map<String, Object> body = Map.of("points", points);

        try {
            log.debug("Qdrant Upsert 요청 JSON:\n{}", objectMapper.writeValueAsString(body));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getJsonHeader());
            restTemplate.put(url, request);
        } catch (Exception e) {
            log.error("Qdrant 벡터 저장 실패", e);
            throw new RuntimeException("Qdrant 벡터 저장 실패", e);
        }
    }

    /* ===================== Delete ===================== */

    /** Qdrant에 저장되어 있는 벡터 데이터 삭제 */
    public void deleteById(Long id) {
        deleteByIds(List.of(id));
    }

    public void deleteByIds(List<Long> ids) {
        final String url = qdrantUrl + "/collections/" + collectionName + "/points/delete?wait=true";

        Map<String, Object> body = Map.of("points", ids);

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getJsonHeader());
            restTemplate.postForEntity(url, request, Map.class);
        } catch (Exception e) {
            log.error("Qdrant 벡터 삭제 실패", e);
            throw new RuntimeException("Qdrant 벡터 삭제 실패", e);
        }
    }

    /* ===================== Query ===================== */

    /** 검색 */
    public List<QueryItemDto> query(
            List<Double> queryTitleVector,
            List<Double> queryCategoryVector,
            Pageable pageable,
            Map<String, Object> filter
    ) {
        final String url = qdrantUrl + "/collections/" + collectionName + "/points/query";

        ensureSize("title", queryTitleVector);
        ensureSize("category", queryCategoryVector);

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();
        int prefetchLimit = offset + limit + Math.min(limit, 50);

        Map<String, Object> body = new HashMap<>();
        body.put("query", queryTitleVector);
        body.put("using", "title");
        body.put("limit", limit);
        body.put("with_payload", true);
        body.put("filter", filter);

//        body.put("prefetch", List.of(
//                Map.of("using", "title", "query", queryTitleVector, "limit", prefetchLimit, "filter", filter),
//                Map.of("using", "category", "query", queryCategoryVector, "limit", prefetchLimit, "filter", filter)
//        ));
//        body.put("query", Map.of("fusion", "rrf"));
//        body.put("offset", offset);
//        body.put("limit", limit);
//        body.put("with_payload", true);
//        body.put("filter", filter);

        try {
            log.debug("Qdrant 검색 요청 JSON:\n{}", objectMapper.writeValueAsString(body));

            // 요청
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getJsonHeader());
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            // 반환값 확인
            Object result = response.getBody() != null ? response.getBody().get("result") : null;
            if (!(result instanceof Map<?, ?>)) return List.of();

            Object points = ((Map<?, ?>) result).get("points");
            if (!(points instanceof List<?>)) return List.of();

            List<Map<String, Object>> hits = (List<Map<String, Object>>) points;

            return hits.stream()
                    .map(hit -> {
                        Long id = ((Number) hit.get("id")).longValue();
                        double score = ((Number) hit.get("score")).doubleValue();
                        Map<String, Object> payload = (Map<String, Object>) hit.get("payload");
                        String title   = payload != null ? String.valueOf(payload.get("title"))   : null;
                        String category= payload != null ? String.valueOf(payload.get("category")): null;
                        return new QueryItemDto(id, title, category, score);
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Qdrant 유사도 검색 실패", e);
        }
    }

    /* ===================== Scroll ===================== */

    /** Point를 가져옴 */
    public ResponseEntity<Map> getPoints(int limit) {
        final String url = qdrantUrl + "/collections/" + collectionName + "/points/scroll";

        Map<String, Object> request = new HashMap<>();
        request.put("limit", limit);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, getJsonHeader());

        return restTemplate.postForEntity(url, entity, Map.class);
    }

    /* ===================== Util ===================== */

    /** JSON 헤더 생성 */
    private HttpHeaders getJsonHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /** 벡터의 차원 크기 검증 */
    private void ensureSize(String name, List<Double> vector) {
        if (vector == null) {
            throw new IllegalArgumentException("벡터가 null입니다: " + name);
        }
        if (vector.size() != vectorSize) {
            throw new IllegalArgumentException(
                    String.format("쿼리 벡터 '%s' 차원 불일치: expected=%d, actual=%d", name, vectorSize, vector.size())
            );
        }
    }
}

