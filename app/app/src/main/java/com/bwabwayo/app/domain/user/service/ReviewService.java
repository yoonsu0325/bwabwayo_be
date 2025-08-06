package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.product.domain.Sale;
import com.bwabwayo.app.domain.product.service.SaleService;
import com.bwabwayo.app.domain.user.domain.*;
import com.bwabwayo.app.domain.user.dto.request.ReviewRequest;
import com.bwabwayo.app.domain.user.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EvaluationService evaluationService;
    private final ReviewEvaluationCountService reviewEvaluationCountService;
    private final ReviewAggService reviewAggService;
    private final SaleService saleService;

    @Transactional
    public void submitReview(ReviewRequest request) {
        // 1. Review 저장
        Review review = reviewRepository.save(ReviewRequest.fromEntity(request));

        //평가 항목들 Map으로 변환해서 저장
        Map<Long, EvaluationItem> itemMap = evaluationService.getAll().stream()
                .collect(Collectors.toMap(EvaluationItem::getId, Function.identity()));

        // 1. 먼저 해당 seller의 모든 review_evaluation_count를 가져와 Map으로 만든다
        Map<Long, ReviewEvaluationCount> countMap = reviewEvaluationCountService
                .getAllByUserId(request.getSellerId()).stream()
                .collect(Collectors.toMap(c -> c.getItem().getId(), Function.identity()));


        for (Long itemId : request.getEvaluationItemsId()) {
            EvaluationItem item = itemMap.get(itemId);
            if (item == null) continue; // 존재하지 않는 항목 무시

            //2. Evaluation 저장
            evaluationService.saveEvaluation(Evaluation.builder()
                    .review(review)
                    .itemId(item)
                    .build());

            //3. Count통계 테이블 업데이트
            // 미리 조회한 Map에서 처리
            ReviewEvaluationCount count = countMap.get(itemId);
            if (count != null) {
                count.increment();
            } else {
                count = ReviewEvaluationCount.builder()
                        .userId(request.getSellerId())
                        .item(item)
                        .count(1)
                        .build();
                reviewEvaluationCountService.saveReviewEvaluationCount(count);
            }
        }

        //4. 총 통계 테이블 업데이트
        reviewAggService.updateReviewAgg(request.getSellerId(), request.getRating());

        //5. sale 리뷰 가능 여부 체크
        Sale sale = saleService.getSaleById(request.getSaleId());
        sale.setReviewed(true);
        saleService.saveSale(sale);


        // 6. (선택) 신뢰도 업데이트 등 추가 로직
//        userService.updateTrustScore(request.getSellerId(), request.getRating());
    }

}
