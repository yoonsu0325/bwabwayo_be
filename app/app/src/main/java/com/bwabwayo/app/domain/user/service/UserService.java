package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.*;
import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat;
import com.bwabwayo.app.domain.user.dto.response.UserInfoResponse;
import com.bwabwayo.app.domain.user.repository.PointRepository;
import com.bwabwayo.app.domain.user.repository.ReviewAggRepository;
import com.bwabwayo.app.domain.user.repository.ReviewEvaluationCountRepository;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final ReviewAggRepository reviewAggRepository;
    private final ReviewEvaluationCountRepository reviewEvaluationCountRepository;

    public User findById(String id){
        return userRepository.findUserById(id);
    }

    public User createUser(UserSignUpRequest request) {
        User user = User.builder()
                .id(request.getId())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profileImage(request.getProfileImage())
                .bio(request.getNickname() + "의 상점입니다.")
                .score(500)
                .point(PointEventType.SIGNUP_FIRST.getPoint())
                .dealCount(0)
                .penaltyCount(0)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .isActive(true)
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public UserInfoResponse getUserInfo(User user) {
        // 기본 정보
        String nickname = user.getNickname();
        String profileImage = user.getProfileImage();
        int score = user.getScore();
        int point = user.getPoint();
        LocalDate createdAt = user.getCreatedAt().toLocalDate();
        String bio = user.getBio();

        // 평점 평균
        float avgRating = reviewAggRepository
                .findByUserId(user.getId())
                .map(ReviewAgg::getAvgRating)
                .orElse(0f);

        // 평가 항목 통계
        List<UserEvaluationStat> evaluations = reviewEvaluationCountRepository
                .findEvaluationStatsByUserId(user.getId());

        return UserInfoResponse.of(nickname, profileImage, score, point, createdAt, bio, avgRating, evaluations);
    }

    @Transactional
    public void calcPoint(PointEventType type, int pointValue, User user) {
        try {
            int currentPoint = user.getPoint();

            // 포인트 부족 예외 체크
            if (currentPoint + pointValue < 0) {
                throw new IllegalStateException("포인트가 부족합니다.");
            }

            // 포인트 이력 저장
            Point point = Point.builder()
                    .userId(user.getId())
                    .type(type)
                    .point(type.isDynamic() ? pointValue : type.getPoint())
                    .build();
            pointRepository.save(point);

            // 포인트 반영
            user.setPoint(currentPoint + pointValue);
            userRepository.save(user);

        } catch (IllegalStateException e) {
            // 여기서 로깅이나 알림 등 예외 처리
            System.out.println("포인트 처리 실패: " + e.getMessage());
            // 필요하다면 다시 던져도 됨
            throw e;
        }
    }
}
