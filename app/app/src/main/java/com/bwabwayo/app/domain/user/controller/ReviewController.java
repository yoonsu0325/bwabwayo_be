package com.bwabwayo.app.domain.user.controller;

import com.bwabwayo.app.domain.user.domain.EvaluationItem;
import com.bwabwayo.app.domain.user.dto.request.ReviewRequest;
import com.bwabwayo.app.domain.user.service.EvaluationItemService;
import com.bwabwayo.app.domain.user.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final EvaluationItemService evaluationItemService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<?> getEvaluationItem(){
        List<EvaluationItem> evaluationItems = evaluationItemService.getEvaluationItems();
        return ResponseEntity.ok(evaluationItems);
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request){
        reviewService.submitReview(request);
        return ResponseEntity.ok("리뷰 등록 완료");
    }
}
