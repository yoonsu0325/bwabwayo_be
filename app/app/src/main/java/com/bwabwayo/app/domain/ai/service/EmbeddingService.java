package com.bwabwayo.app.domain.ai.service;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.SimilarResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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


    private HttpHeaders getJsonHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    // Qdrant에 벡터 저장 (upsert 방식)
    public void saveToQdrant(QdrantPointDto dto) {
        final String url = qdrantUrl + "/collections/" + collectionName + "/points?wait=true";

        // Qdrant에 보낼 포맷 구성
        Map<String, Object> vectors = new HashMap<>();
        vectors.put("title", dto.getTitleVector());
        vectors.put("category", dto.getCategoryVector());

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", dto.getTitle());
        payload.put("category", dto.getCategory());

        Map<String, Object> point = new HashMap<>();
        point.put("id", dto.getId());
        point.put("vectors", vectors);
        point.put("payload", payload);

        // points 배열 하나만 포함
        Map<String, Object> body = Map.of("points", List.of(point));

        try {
            log.info("Qdrant 저장 요청 JSON:\n{}", objectMapper.writeValueAsString(body));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getJsonHeader());

            restTemplate.put(url, request);
        } catch (Exception e) {
            throw new RuntimeException("Qdrant 벡터 저장 실패", e);
        }
    }

    // 유사도 검증
    public List<SimilarResultResponse> searchSimilarTitles(
            List<Double> queryTitleVec,
            List<Double> queryCategoryVec,
            int topK
    ) {
        // 1) 각각 검색 (여유 있게 topK*5)
        List<Map<String, Object>> titleHits = searchOnce("title", queryTitleVec, topK * 5);
        List<Map<String, Object>> categoryHits = searchOnce("category", queryCategoryVec, topK * 5);

        // 2) 가중치 합성(예: title 0.8, category 0.2)
        double wTitle = 0.8;
        double wCategory = 0.2;

        Map<Long, Double> fusedScore = new HashMap<>();
        Map<Long, Map<String, Object>> payloadById = new HashMap<>();

        for (Map<String, Object> h : titleHits) {
            Long id = ((Number) h.get("id")).longValue();
            double score = ((Number) h.get("score")).doubleValue();
            fusedScore.merge(id, score * wTitle, Double::sum);
            if (!payloadById.containsKey(id)) {
                payloadById.put(id, (Map<String, Object>) h.get("payload"));
            }
        }
        for (Map<String, Object> h : categoryHits) {
            Long id = ((Number) h.get("id")).longValue();
            double score = ((Number) h.get("score")).doubleValue();
            fusedScore.merge(id, score * wCategory, Double::sum);
            // payload가 비어있으면 채워둠
            payloadById.putIfAbsent(id, (Map<String, Object>) h.get("payload"));
        }

        // 3) 정렬 후 상위 topK 반환
        return fusedScore.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(e -> {
                    Long id = e.getKey();
                    double score = e.getValue();
                    Map<String, Object> payload = payloadById.get(id);
                    String title = payload != null ? String.valueOf(payload.get("title")) : null;
                    String category = payload != null ? String.valueOf(payload.get("category")) : null;
                    return new SimilarResultResponse(id, title, category, score);
                })
                .toList();
    }

    // Qdrant에 저장되어 있는 벡터 데이터 삭제
    public void deleteFromQdrantById(Long id) {
        final String url = qdrantUrl + "/collections/" + collectionName + "/points/delete?wait=true";
        // 올바른 JSON 구조: points: [id]
        Map<String, Object> body = Map.of("points", List.of(id));

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getJsonHeader());
            restTemplate.postForEntity(url, request, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Qdrant 벡터 삭제 실패", e);
        }
    }


    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchOnce(String using, List<Double> vector, int limit) {
        final String url = qdrantUrl + "/collections/" + collectionName + "/points/search";

        Map<String, Object> vectorObj = new HashMap<>();
        vectorObj.put("name", using);
        vectorObj.put("vector", toFloatList(vector));

        Map<String, Object> body = new HashMap<>();
        body.put("vector", vectorObj);
        body.put("limit", limit);
        body.put("with_payload", true);

        try {
            log.info("Qdrant 검색 요청 JSON (using={}):\n{}", using, objectMapper.writeValueAsString(body));
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getJsonHeader());
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Object result = response.getBody() != null ? response.getBody().get("result") : null;
            return result instanceof List ? (List<Map<String, Object>>) result : List.of();
        } catch (Exception e) {
            throw new RuntimeException("Qdrant 유사도 검색 실패", e);
        }
    }

    private List<Float> toFloatList(List<Double> src) {
        return src == null ? List.of() : src.stream().map(Double::floatValue).toList();
    }

    public ResponseEntity<Map> getPoints(int limit) {
        String url = qdrantUrl + "/collections/" + collectionName + "/points/scroll";

        Map<String, Object> request = new HashMap<>();
        request.put("limit", limit);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(request, getJsonHeader());

        return restTemplate.postForEntity(url, entity, Map.class);
    }
}

