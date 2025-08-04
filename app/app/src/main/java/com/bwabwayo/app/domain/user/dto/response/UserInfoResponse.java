package com.bwabwayo.app.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class UserInfoResponse {
    private String user_id;
    private int status;
    private String message;
    private String nickname;
    private String profile_image;
    private int score;
    private int point;
    private LocalDate created_at;
    private String bio;
    private float rating;
    private List<UserEvaluationStat> evaluation;

    public static UserInfoResponse of(String user_id, String nickname, String profileImage, int score, int point,
                                      LocalDate createdAt, String bio, float rating, List<UserEvaluationStat> evaluation) {
        return UserInfoResponse.builder()
                .status(200)
                .message("회원 정보 조회에 성공하였습니다")
                .user_id(user_id)
                .nickname(nickname)
                .profile_image(profileImage)
                .score(score)
                .point(point)
                .created_at(createdAt)
                .bio(bio)
                .rating(rating)
                .evaluation(evaluation)
                .build();
    }
}

