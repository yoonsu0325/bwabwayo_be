package com.bwabwayo.app.domain.support.dto.request;

import com.bwabwayo.app.domain.support.domain.Inquery;
import com.bwabwayo.app.domain.support.domain.Report;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    private String title;
    private String description;
    private User target;
    private String imageUrl;

    public static Report toEntity(ReportRequest request){
        return Report.builder()
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .target(request.getTarget())
                .build();
    }
}
