package com.bwabwayo.app.domain.support.service;

import com.bwabwayo.app.domain.support.domain.Report;
import com.bwabwayo.app.domain.support.domain.ReportImage;
import com.bwabwayo.app.domain.support.dto.request.ReportRequest;
import com.bwabwayo.app.domain.support.dto.response.ReportResponse;
import com.bwabwayo.app.domain.support.repository.ReportRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.global.storage.service.StorageService;
import com.bwabwayo.app.global.storage.util.StorageUtil;
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
    private final StorageService storageService;
    private final StorageUtil storageUtil;

    // 신고 게시물 페이지 페이징
    public Page<ReportResponse> findAll(Pageable pageable) {

        Page<Report> reports = reportRepository.findAll(pageable);

        return reports.map(Report ->
                ReportResponse.builder()
                        .id(Report.getId())
                        .title(Report.getTitle())
                        .imageUrlList(Report.getReportImages().stream().map(img->storageService.getUrlFromKey(img.getUrl())).toList())
                        .targetName(Report.getTarget().getNickname())
                        .name(Report.getReporter().getNickname())
                        .description(Report.getDescription())
                        .reply(Report.getReply())
                        .createdAt(Report.getCreatedAt().toString())
                        .build());
    }

    public Page<ReportResponse> findReportByReportId(User user, Pageable pageable) {
        Page<Report> reports = reportRepository.findReportsByReporter_Id(user.getId(), pageable);

        return reports.map(Report ->
                ReportResponse.builder()
                        .id(Report.getId())
                        .title(Report.getTitle())
                        .imageUrlList(Report.getReportImages().stream().map(img->storageService.getUrlFromKey(img.getUrl())).toList())
                        .targetName(Report.getTarget().getNickname())
                        .name(Report.getReporter().getNickname())
                        .description(Report.getDescription())
                        .reply(Report.getReply())
                        .createdAt(Report.getCreatedAt().toString())
                        .build());
    }

    // 신고 게시물 작성
    public String save(ReportRequest reportRequest, User user) {
        Report report = Report.builder()
                .title(reportRequest.getTitle())
                .description(reportRequest.getDescription())
                .target(reportRequest.getTarget())
                .reporter(user)
                .build();

        storageUtil.copyToDirectory(reportRequest.getImageUrlList(), "temp", "reports");

        int index = 1;
        for (String imageUrl : reportRequest.getImageUrlList()) {
            report.getReportImages().add(
                    ReportImage.builder()
                            .no(index++)
                            .report(report)
                            .url(imageUrl)
                            .build()
            );
        }

        reportRepository.save(report);

        return "게시물이 저장되었습니다.";
    }
}
