package com.bwabwayo.app.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class UserInfoResponse {
    private String nickname;
    private String profileImage;
    private int score;
    private int point;
    private LocalDate createdAt;
    private String bio;
    private float rating;
    private int reviewCount;
    private List<UserEvaluationStat> evaluation;
}

