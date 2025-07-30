package com.bwabwayo.app.domain.ai.service;

import com.bwabwayo.app.domain.ai.domain.QdrantPointDto;
import com.bwabwayo.app.domain.ai.dto.response.SimilarResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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



    // Qdrant에 벡터 저장 (upsert 방식)
    public void saveToQdrant(QdrantPointDto dto) {
        try {
            String url = qdrantUrl + "/collections/" + collectionName + "/points?wait=true";

            // Qdrant에 보낼 포맷 구성
            Map<String, Object> point = new HashMap<>();
            point.put("id", dto.getId());
            point.put("vector", dto.getVector());

            Map<String, Object> payload = new HashMap<>();
            payload.put("title", dto.getTitle());
            point.put("payload", payload);

            // points 배열 하나만 포함
            Map<String, Object> body = Map.of("points", List.of(point));

            System.out.println("Qdrant 저장 요청 JSON:\n" + objectMapper.writeValueAsString(body));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.put(url, request);

        } catch (Exception e) {
            throw new RuntimeException("Qdrant 벡터 저장 실패", e);
        }
    }



    // 유사도 검증
    public List<SimilarResultResponse> searchSimilarTitles(List<Double> queryVector, int topK) {
        try {
            String url = qdrantUrl + "/collections/" + collectionName + "/points/search";

            Map<String, Object> body = new HashMap<>();
            body.put("vector", queryVector);
            body.put("limit", topK);
            body.put("with_payload", true);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("result");

            return results.stream()
                    .map(r -> {
                        Object idObj = r.get("id");
                        long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : -1L;

                        Map<String, Object> payload = (Map<String, Object>) r.get("payload");
                        String title = payload != null ? (String) payload.get("title") : null;

                        return new SimilarResultResponse(id, title);
                    })
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Qdrant 유사도 검색 실패", e);
        }
    }

    // Qdrant에 저장되어 있는 벡터 데이터 삭제
    public void deleteFromQdrantById(Long id) {
        try {
            String url = qdrantUrl + "/collections/" + collectionName + "/points/delete?wait=true";

            // 올바른 JSON 구조: points: [id]
            Map<String, Object> body = Map.of("points", List.of(id));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Qdrant 벡터 삭제 실패", e);
        }
    }





}

