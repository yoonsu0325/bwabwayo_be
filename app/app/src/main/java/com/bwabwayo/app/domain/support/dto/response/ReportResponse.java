package com.bwabwayo.app.domain.support.dto.response;

import com.bwabwayo.app.domain.support.domain.Inquery;
import com.bwabwayo.app.domain.support.domain.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String title;
    private String targetName;
    private String description;
    private String reply;
    private String createdAt;

}
