package com.bwabwayo.app.domain.support.service;

import com.bwabwayo.app.domain.support.domain.Report;
import com.bwabwayo.app.domain.support.dto.request.ReportRequest;
import com.bwabwayo.app.domain.support.dto.response.ReportResponse;
import com.bwabwayo.app.domain.support.repository.ReportRepository;
import com.bwabwayo.app.domain.user.domain.User;
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

        return reports.map(Report ->
                ReportResponse.builder()
                        .id(Report.getId())
                        .title(Report.getTitle())
                        .imageUrl(Report.getImageUrl())
                        .targetName(Report.getTarget().getNickname())
                        .description(Report.getDescription())
                        .reply(Report.getReply())
                        .createdAt(Report.getCreatedAt().toString())
                        .build());
    }

    // 신고 게시물 작성
    public String save(ReportRequest reportRequest, User user) {
        Report report = reportRequest.toEntity(reportRequest);
        report.setReporter(user);
        reportRepository.save(report);

        return "게시물이 저장되었습니다.";
    }
}
