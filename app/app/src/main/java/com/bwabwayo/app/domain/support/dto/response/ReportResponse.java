package com.bwabwayo.app.domain.support.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String title;
    private List<String> imageUrlList;
    private String targetName;
    private String name;
    private String description;
    private String reply;
    private String createdAt;
    private String repliedAt;

}
