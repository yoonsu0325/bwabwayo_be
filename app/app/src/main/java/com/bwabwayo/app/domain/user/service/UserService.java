package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.product.exception.NotFoundException;
import com.bwabwayo.app.domain.user.domain.*;
import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat;
import com.bwabwayo.app.domain.user.dto.response.UserInfoResponse;
import com.bwabwayo.app.domain.user.repository.PointRepository;
import com.bwabwayo.app.domain.user.repository.ReviewAggRepository;
import com.bwabwayo.app.domain.user.repository.ReviewEvaluationCountRepository;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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
        String userId = user.getId();
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

        return UserInfoResponse.of(userId, nickname, profileImage, score, point, createdAt, bio, avgRating, evaluations);
    }

    public UserInfoResponse getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저가 없습니다."));
        return getUserInfo(user); // 재사용
    }

    @Retryable(
            value = { OptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100) // 100ms 간격 재시도
    )
    @Transactional
    public void calcPoint(PointEventType type, int pointValue, User user) {
        int currentPoint = user.getPoint();

        if (currentPoint + pointValue < 0) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        Point point = Point.builder()
                .userId(user.getId())
                .type(type)
                .point(type.isDynamic() ? pointValue : type.getPoint())
                .build();
        pointRepository.save(point);

        user.setPoint(currentPoint + pointValue);
        userRepository.save(user); // OptimisticLock 충돌 시 여기서 예외 발생
    }

    // 재시도 실패한 경우 fallback
    @Recover
    public void recover(OptimisticLockingFailureException e, PointEventType type, int pointValue, User user) {
        System.out.println("재시도 3회 실패: " + e.getMessage());
        throw new IllegalStateException("포인트 처리에 실패했습니다. 다시 시도해주세요.");
    }
}
