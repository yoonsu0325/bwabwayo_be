package com.bwabwayo.app.domain.ai.util;

import java.util.*;

public class QdrantFilterBuilder {
    private final List<Map<String, Object>> must = new ArrayList<>();
    private final List<Map<String, Object>> should = new ArrayList<>();
    private final List<Map<String, Object>> mustNot = new ArrayList<>();

    /** key=value 조건 */
    public QdrantFilterBuilder match(String key, Object value) {
        must.add(Map.of(
            "key", key,
            "match", Map.of("value", value)
        ));
        return this;
    }

    /** key in [values...] 조건 */
    public QdrantFilterBuilder in(String key, Collection<?> values) {
        must.add(Map.of(
            "key", key,
            "match", Map.of("any", values)
        ));
        return this;
    }

    /** key 범위 조건 */
    public QdrantFilterBuilder range(String key, Number gte, Number lte) {
        Map<String, Object> range = new HashMap<>();
        if (gte != null) range.put("gte", gte);
        if (lte != null) range.put("lte", lte);

        must.add(Map.of(
            "key", key,
            "range", range
        ));
        return this;
    }

    /** OR 조건 추가 */
    public QdrantFilterBuilder orMatch(String key, Object value) {
        should.add(Map.of(
            "key", key,
            "match", Map.of("value", value)
        ));
        return this;
    }

    /** NOT 조건 추가 */
    public QdrantFilterBuilder notMatch(String key, Object value) {
        mustNot.add(Map.of(
            "key", key,
            "match", Map.of("value", value)
        ));
        return this;
    }

    /** 최종 Map 생성 */
    public Map<String, Object> build() {
        Map<String, Object> filter = new HashMap<>();
        if (!must.isEmpty()) filter.put("must", must);
        if (!should.isEmpty()) filter.put("should", should);
        if (!mustNot.isEmpty()) filter.put("must_not", mustNot);
        return filter.isEmpty() ? null : filter;
    }
}
