package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.product.exception.NotFoundException;
import com.bwabwayo.app.domain.user.domain.*;
import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.dto.request.UserDetailRequest;
import com.bwabwayo.app.domain.user.dto.response.UserDetailResponse;
import com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat;
import com.bwabwayo.app.domain.user.dto.response.UserInfoResponse;
import com.bwabwayo.app.domain.user.repository.*;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final ReviewAggRepository reviewAggRepository;
    private final ReviewEvaluationCountRepository reviewEvaluationCountRepository;
    private final AccountRepository accountRepository;

    public User findById(String id){
        return userRepository.findUserById(id);
    }

    public User createUser(UserSignUpRequest request) {
        User user = User.builder()
                .id(request.getId())
                .nickname(request.getNickname())
                .version(0L)
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

    public UserInfoResponse getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저가 없습니다."));
        return getUserInfo(user); // 재사용
    }

    public UserDetailResponse getUserDetail(User user){
        Account account = accountRepository.findByUserId(user.getId());
        return UserDetailResponse.of(
                user.getNickname(),
                user.getProfileImage(),
                user.getBio(),
                Optional.ofNullable(account).map(Account::getAccountNumber).orElse(null),
                Optional.ofNullable(account).map(Account::getBankName).orElse(null),
                Optional.ofNullable(account).map(Account::getAccountHolder).orElse(null)
        );
    }

    public void updateUserDetail(UserDetailRequest request, User user) {
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
//        user.setProfileImage(request.getProfileImage()); 따로 S3에 맞게 코딩해야함
        boolean hasAllAccountFields = request.getAccountNumber() != null &&
                request.getBankName() != null &&
                request.getAccountHolder() != null;

        if (hasAllAccountFields) {
            Account account = accountRepository.findByUserId(user.getId());

            if (account != null) {
                // 기존 계좌 수정
                account.setAccountNumber(request.getAccountNumber());
                account.setBankName(request.getBankName());
                account.setAccountHolder(request.getAccountHolder());
            } else {
                // 계좌 신규 등록
                account = Account.builder()
                        .user(user)
                        .accountNumber(request.getAccountNumber())
                        .bankName(request.getBankName())
                        .accountHolder(request.getAccountHolder())
                        .build();
            }

            accountRepository.save(account);
        }
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
