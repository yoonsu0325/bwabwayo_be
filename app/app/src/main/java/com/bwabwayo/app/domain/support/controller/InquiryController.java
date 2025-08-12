package com.bwabwayo.app.domain.support.controller;

import com.bwabwayo.app.domain.support.dto.request.InquiryRequest;
import com.bwabwayo.app.domain.support.dto.response.InquiryResponse;
import com.bwabwayo.app.domain.support.service.InquiryService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
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
@RequestMapping("api/support/inquery")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InquiryController {

    private final InquiryService inquiryService;

    // 문의 게시물 페이지 페이징
    @GetMapping
    public ResponseEntity<Page<InquiryResponse>> getInquiryAll(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(inquiryService.findAll(pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<InquiryResponse>> getInquiryByUserId(
            @LoginUser User user,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(inquiryService.findInquiryByUserId(user, pageable));
    }

    // 문의 게시물 작성
    @PostMapping("/save")
    public ResponseEntity<?> saveInquiry(@RequestBody InquiryRequest inqueryRequest, @LoginUser User user) {
        inquiryService.save(inqueryRequest, user);
        return ResponseEntity.ok().build();
    }


}
