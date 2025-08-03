package com.bwabwayo.app.domain.support.controller;


import com.bwabwayo.app.domain.support.dto.request.ReportRequest;
import com.bwabwayo.app.domain.support.dto.response.ReportResponse;
import com.bwabwayo.app.domain.support.service.ReportService;
import com.bwabwayo.app.domain.user.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support/report")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportController {

    private final ReportService reportService;

    // 신고 게시물 페이지 페이징
    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getInqueryAll(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(reportService.findAll(pageable));
    }

    // 문의 게시물 작성
    @PostMapping("/save")
    public ResponseEntity<?> saveReport(@RequestBody ReportRequest reportRequest, @LoginUser User user) {
        String result =  reportService.save(reportRequest, user);
        return ResponseEntity.ok().body(result);
    }
}
