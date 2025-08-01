package com.bwabwayo.app.domain.support.controller;

import com.bwabwayo.app.domain.support.dto.request.InqueryRequest;
import com.bwabwayo.app.domain.support.dto.response.InqueryResponse;
import com.bwabwayo.app.domain.support.service.InqueryService;
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
@RequestMapping("api/support/inquery")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InqueryController {

    private final InqueryService inqueryService;

    // 문의 게시물 페이지 페이징
    @GetMapping
    public ResponseEntity<Page<InqueryResponse>> getInqueryAll(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(inqueryService.findAll(pageable));
    }

    // 문의 게시물 작성
    @PostMapping("/save")
    public ResponseEntity<?> saveInquery(@RequestBody InqueryRequest inqueryRequest, @LoginUser User user) {
        String result =  inqueryService.save(inqueryRequest, user);
        return ResponseEntity.ok().body(result);
    }


}
