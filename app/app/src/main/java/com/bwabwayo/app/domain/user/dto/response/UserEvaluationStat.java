package com.bwabwayo.app.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserEvaluationStat {
    @JsonProperty("item_id")
    private Long itemId;

    private String description;

    @JsonProperty("number")
    private int count;
}

