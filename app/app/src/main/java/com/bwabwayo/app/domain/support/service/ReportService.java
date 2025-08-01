package com.bwabwayo.app.domain.support.service;

import com.bwabwayo.app.domain.support.domain.Inquery;
import com.bwabwayo.app.domain.support.domain.Report;
import com.bwabwayo.app.domain.support.dto.response.InqueryResponse;
import com.bwabwayo.app.domain.support.dto.response.ReportResponse;
import com.bwabwayo.app.domain.support.repository.InqueryRepository;
import com.bwabwayo.app.domain.support.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;

    // 신고 게시물 페이지 페이징
    public Page<ReportResponse> findAll(Pageable pageable) {

        Page<Report> reports = reportRepository.findAll(pageable);

        // Inquery response 객체로 변환
        return reports.map(Report ->
                ReportResponse.builder()
                        .id(Report.getId())
                        .title(Report.getTitle())
                        .targetName(Report.getTarget().getNickname())
                        .description(Report.getDescription())
                        .reply(Report.getReply())
                        .createdAt(Report.getCreatedAt().toString())
                        .build());
    }
}
