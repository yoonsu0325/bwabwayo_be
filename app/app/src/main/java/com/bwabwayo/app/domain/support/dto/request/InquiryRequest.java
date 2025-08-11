package com.bwabwayo.app.domain.support.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequest {

    private String title;
    private String description;
    private List<String> imageUrlList;

}
