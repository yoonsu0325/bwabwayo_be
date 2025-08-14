package com.bwabwayo.app.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SecondhandTextPreprocessingService {
    
    private final Analyzer koreanAnalyzer;
    private final Map<String, String> brandNormalizations;
    private final Map<String, String> conditionNormalizations;
    private final Map<String, String> abbreviationExpansions;
    private final Set<String> stopWords;
    private final Pattern sizePattern;
    private final Pattern pricePattern;
    private final Pattern modelPattern;
    
    public SecondhandTextPreprocessingService() {
        this.koreanAnalyzer = new KoreanAnalyzer();
        this.brandNormalizations = initializeBrandNormalizations();
        this.conditionNormalizations = initializeConditionNormalizations();
        this.abbreviationExpansions = initializeAbbreviationExpansions();
        this.stopWords = initializeStopWords();
        this.sizePattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(ml|l|리터|미리리터|gb|기가|테라|tb|인치|cm|mm|kg|g|그램|킬로)(?![a-z가-힣])", Pattern.CASE_INSENSITIVE);
        this.pricePattern = Pattern.compile("\\d+(?:만원?|원|천원?)");
        this.modelPattern = Pattern.compile("(iphone|아이폰)\\s*(\\d+)(?:\\s*(pro|max|plus|mini))*", Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * 중고거래 특화 텍스트 전처리
     */
    public String preprocessForSecondhand(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String original = text;
        
        try {
            // 1. 기본 정리 및 정규화
            text = normalizeText(text);
            
            // 2. 브랜드명 정규화
            text = normalizeBrands(text);
            
            // 3. 상품 상태 정규화
            text = normalizeConditions(text);
            
            // 4. 줄임말 확장
            text = expandAbbreviations(text);
            
            // 5. 크기/용량 정규화
            text = normalizeSizeAndCapacity(text);
            
            // 6. 모델명 정규화
            text = normalizeModelNames(text);
            
            // 7. 형태소 분석 및 토큰화
            List<String> tokens = analyzeWithNori(text);
            
            // 8. 토큰 후처리
            tokens = postProcessTokens(tokens);

            String result = String.join(" ", tokens);
            log.info("텍스트 전처리: text={}, tokens={}", original, result);
            return result;
        } catch (Exception e) {
            log.info("텍스트 전처리: text={}, tokens={}", original, text);
            return text.trim(); // 실패 시 원본 반환
        }
    }
    
    /**
     * 기본 텍스트 정규화
     */
    private String normalizeText(String text) {
        return text
                // 특수문자를 공백으로 변경 (단, 브랜드/모델에 자주 사용되는 것은 제외)
                .replaceAll("[^\\w\\s가-힣+\\-.]", " ")
                // 연속된 공백 제거
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
    
    /**
     * 브랜드명 정규화
     */
    private String normalizeBrands(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : brandNormalizations.entrySet()) {
            result = result.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return result;
    }
    
    /**
     * 상품 상태 정규화
     */
    private String normalizeConditions(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : conditionNormalizations.entrySet()) {
            result = result.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return result;
    }
    
    /**
     * 줄임말 확장
     */
    private String expandAbbreviations(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : abbreviationExpansions.entrySet()) {
            result = result.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return result;
    }
    
    /**
     * 크기/용량 정규화 (단위 통일)
     */
    private String normalizeSizeAndCapacity(String text) {
        Matcher matcher = sizePattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            
            // 단위 정규화
            String normalizedUnit = normalizeUnit(unit, value);
            matcher.appendReplacement(sb, value + normalizedUnit);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 모델명 정규화 (iPhone 예시)
     */
    private String normalizeModelNames(String text) {
        Matcher matcher = modelPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String brand = "아이폰";
            String model = matcher.group(2);
            String variant = matcher.group(3) != null ? " " + matcher.group(3).toLowerCase() : "";
            
            matcher.appendReplacement(sb, brand + " " + model + variant);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Nori를 사용한 형태소 분석
     */
    private List<String> analyzeWithNori(String text) throws IOException {
        List<String> tokens = new ArrayList<>();
        
        try (TokenStream tokenStream = koreanAnalyzer.tokenStream("content", new StringReader(text))) {
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            
            while (tokenStream.incrementToken()) {
                String token = termAttribute.toString();
                if (isValidToken(token)) {
                    tokens.add(token);
                }
            }
            
            tokenStream.end();
        }
        
        return tokens;
    }
    
    /**
     * 토큰 후처리
     */
    private List<String> postProcessTokens(List<String> tokens) {
        return tokens.stream()
                .filter(token -> !stopWords.contains(token))
//                .filter(token -> token.length() >= 2 || isNumericOrSize(token))
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 유효한 토큰인지 검사
     */
    private boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        if (token.length() == 1 && !Character.isDigit(token.charAt(0))) return false;
        return !pricePattern.matcher(token).matches(); // 가격 정보는 제외
    }
    
    /**
     * 숫자나 크기 단위인지 확인
     */
    private boolean isNumericOrSize(String token) {
        return token.matches("\\d+") || 
               token.matches("\\d+(?:gb|tb|ml|l|인치|cm|mm)");
    }
    
    /**
     * 단위 정규화
     */
    private String normalizeUnit(String unit, double value) {
        switch (unit.toLowerCase()) {
            case "ml": case "미리리터": return "ml";
            case "l": case "리터": return "l";
            case "gb": case "기가": return "gb";
            case "tb": case "테라": return "tb";
            case "kg": case "킬로": return "kg";
            case "g": case "그램": return "g";
            case "cm": return "cm";
            case "mm": return "mm";
            case "인치": return "인치";
            default: return unit;
        }
    }
    
    /**
     * 브랜드명 정규화 맵 초기화
     */
    private Map<String, String> initializeBrandNormalizations() {
        Map<String, String> map = new HashMap<>();
        // 전자제품
        map.put("삼성|samsung", "삼성");
        map.put("애플|apple", "애플");
        map.put("엘지|lg", "엘지");
        map.put("아이폰|iphone", "아이폰");
        map.put("갤럭시|galaxy", "갤럭시");
        map.put("맥북|macbook", "맥북");
        map.put("아이패드|ipad", "아이패드");
        
        // 패션 브랜드
        map.put("나이키|nike", "나이키");
        map.put("아디다스|adidas", "아디다스");
        map.put("유니클로|uniqlo", "유니클로");
        map.put("자라|zara", "자라");
        
        // 자동차
        map.put("현대|hyundai", "현대");
        map.put("기아|kia", "기아");
        map.put("벤츠|mercedes", "벤츠");
        map.put("bmw|비엠더블유", "bmw");
        
        return map;
    }
    
    /**
     * 상품 상태 정규화 맵 초기화
     */
    private Map<String, String> initializeConditionNormalizations() {
        Map<String, String> map = new HashMap<>();
        map.put("새상품|미개봉|새제품", "새상품");
        map.put("거의새것|거의새상품|거의안씀", "거의새것");
        map.put("상급|상태좋음|깨끗함", "상급");
        map.put("중급|보통|일반사용감", "중급");
        map.put("하급|사용감많음|낡음", "하급");
        map.put("고장|파손|수리필요", "수리필요");
        return map;
    }
    
    /**
     * 줄임말 확장 맵 초기화
     */
    private Map<String, String> initializeAbbreviationExpansions() {
        Map<String, String> map = new HashMap<>();
        // 전자제품
        map.put("노트북|nb", "노트북");
        map.put("스마트폰|폰", "스마트폰");
        map.put("태블릿|태블", "태블릿");
        map.put("에어팟|에어팟츠", "에어팟");
        map.put("케이스|케이스", "케이스");
        
        // 의류
        map.put("티셔츠|티", "티셔츠");
        map.put("후드티|후드", "후드티");
        map.put("청바지|청", "청바지");
        map.put("운동화|운화", "운동화");
        
        // 생활용품
        map.put("화장품|코스메틱", "화장품");
        map.put("향수|퍼퓸", "향수");
        map.put("가방|백", "가방");
        
        // 상태 관련
        map.put("새거|새것", "새상품");
        map.put("중고|헌거", "중고");
        
        return map;
    }
    
    /**
     * 불용어 초기화
     */
    private Set<String> initializeStopWords() {
        return Set.of(
                // 한국어 불용어
                "이", "그", "저", "것", "들", "에서", "으로", "를", "을", "는", "은",
                "가", "의", "와", "과", "에", "도", "만", "부터", "까지", "에게",
                "한테", "보고", "하고", "에다", "으며", "며", "이며", "인데", "라는",
                
                // 영어 불용어
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "with", "by", "is", "are", "was", "were", "be", "been", "have", "has",
                
                // 중고거래 관련 불용어
                "판매", "팔아요", "팔아여", "급처", "급매", "택배", "직거래", "네고",
                "가능", "문의", "연락", "카톡", "채팅", "댓글", "구매", "사요", "사실",
                "분들", "하시는", "원하시는", "필요하신", "찾으시는"
        );
    }
    
    /**
     * 리소스 정리
     */
    public void close() throws IOException {
        if (koreanAnalyzer != null) {
            koreanAnalyzer.close();
        }
    }
}