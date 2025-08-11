package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.domain.*;
import com.bwabwayo.app.domain.user.dto.request.UserDetailRequest;
import com.bwabwayo.app.domain.user.dto.response.UserDetailResponse;
import com.bwabwayo.app.domain.user.dto.response.UserEvaluationStat;
import com.bwabwayo.app.domain.user.dto.response.UserInfoResponse;
import com.bwabwayo.app.domain.user.repository.ReviewAggRepository;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.global.exception.NotFoundException;
import com.bwabwayo.app.global.storage.service.StorageService;
import com.bwabwayo.app.global.storage.util.StorageUtil;
import com.bwabwayo.app.global.url.URLValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final ReviewAggRepository reviewAggRepository;
    private final StorageUtil storageUtil;
    private final StorageService storageService;
    private final AccountService accountService;
    private final ReviewAggService reviewAggService;
    private final ReviewEvaluationCountService reviewEvaluationCountService;
    private final PointService pointService;

    // ====== 신뢰도 계산 ======
    private static final double BASE = 500.0;
    private static final double K_T = 120.0;
    private static final double T_MAX = 240.0;
    private static final double K_R = 40.0;
    private static final double RATE_MAX = 210.0;
    private static final double R_MAX = 60.0;
    private static final double PENALTY_UNIT = 40.0;
    private static final double PENALTY_CAP = 200.0;

    private static final int SCORE_MIN = 0;
    private static final int SCORE_MAX = 1000;

    @Value("${storage.path.temp}")
    private String tempPath;
    @Value("${storage.path.profileImage}")
    private String profilePath;

    public User findById(String id){
        return userRepository.findUserById(id);
    }

    public User createUser(UserSignUpRequest request) {
        String profileImage = request.getProfileImage();
        String targetKey;
        if(profileImage == null || profileImage.isEmpty()){
            targetKey = "profiles/20250811162152_6cccbdf7-b8be-4ca9-adea-09bba6a90e1b.png";
        } else {
            if (URLValidator.isValidURL(request.getProfileImage())) { // 다운로드 후 S3 업로드
                targetKey = storageService.upload(profileImage, profilePath);
            } else { // S3의 profile로 이동
                targetKey = storageUtil.copyToDirectory(profileImage, tempPath, profilePath);
            }
        }


        User user = User.builder()
                .id(request.getId())
                .nickname(request.getNickname())
                .version(null)
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profileImage(targetKey)
                .bio(request.getNickname() + "의 상점입니다.")
                .score(500)
                .point(0)
                .dealCount(0)
                .penaltyCount(0)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .lastLoginAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .isActive(true)
                .role(Role.USER)
                .build();
        User returnUser = userRepository.save(user);
        userRepository.flush();
        return returnUser;
    }

    public User updateUser(User user, UserSignUpRequest request) {
        String profileImage = request.getProfileImage();
        String targetKey;
        if(profileImage == null || profileImage.isEmpty()){
            targetKey = "profiles/20250811162152_6cccbdf7-b8be-4ca9-adea-09bba6a90e1b.png";
        } else {
            if (URLValidator.isValidURL(request.getProfileImage())) { // 다운로드 후 S3 업로드
                targetKey = storageService.upload(profileImage, profilePath);
            } else { // S3의 profile로 이동
                targetKey = storageUtil.copyToDirectory(profileImage, tempPath, profilePath);
            }
        }

        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfileImage(targetKey);
        user.setBio(request.getNickname() + "의 상점입니다.");
        user.setScore(500);
        user.setPoint(0);
        user.setDealCount(0);
        user.setPenaltyCount(0);
        user.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        user.setLastLoginAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        user.setActive(true);
        user.setRole(Role.USER);
        User returnUser = userRepository.save(user);
        userRepository.flush();
        return returnUser;
    }

    public UserInfoResponse getUserInfo(User user) {
        // 기본 정보
        String nickname = user.getNickname();
        String profileImage = storageService.getUrlFromKey(user.getProfileImage());
        int score = user.getScore();
        int point = user.getPoint();
        LocalDate createdAt = user.getCreatedAt().toLocalDate();
        String bio = user.getBio();

        // 평점 평균
        float avgRating = reviewAggService.getAvgRating(user.getId());

        //리뷰 개수
        int reviewCount = reviewAggService.getReviewCount(user.getId());

        // 평가 항목 통계
        List<UserEvaluationStat> evaluations = reviewEvaluationCountService
                .getEvaluationStats(user.getId());

        return UserInfoResponse.builder()
                .nickname(nickname)
                .profileImage(profileImage)
                .score(score)
                .point(point)
                .createdAt(createdAt)
                .bio(bio)
                .rating(avgRating)
                .reviewCount(reviewCount)
                .evaluation(evaluations)
                .build();
    }

    public UserInfoResponse getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저가 없습니다."));
        return getUserInfo(user); // 재사용
    }

    public UserDetailResponse getUserDetail(User user){
        Account account = accountService.getAccount(user.getId());
        return UserDetailResponse.builder()
                .nickname(user.getNickname())
                .profileImage(storageService.getUrlFromKey(user.getProfileImage()))
                .bio(user.getBio())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .accountNumber(Optional.ofNullable(account).map(Account::getAccountNumber).orElse(null))
                .bankName(Optional.ofNullable(account).map(Account::getBankName).orElse(null))
                .accountHolder(Optional.ofNullable(account).map(Account::getAccountHolder).orElse(null))
                .build();
    }

    @Transactional
    public void updateUserDetail(UserDetailRequest request, User user) {
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        String profileImage = request.getProfileImage();
        String targetKey;
        if(profileImage == null || profileImage.isEmpty()){
            targetKey = "profiles/20250811162152_6cccbdf7-b8be-4ca9-adea-09bba6a90e1b.png";
        } else {
            if (URLValidator.isValidURL(request.getProfileImage())) { // 다운로드 후 S3 업로드
                targetKey = storageService.upload(profileImage, profilePath);
            } else { // S3의 profile로 이동
                targetKey = storageUtil.copyToDirectory(profileImage, tempPath, profilePath);
            }
        }
        user.setProfileImage(targetKey);

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        userRepository.saveAndFlush(user);


        boolean hasAllAccountFields = request.getAccountNumber() != null &&
                request.getBankName() != null &&
                request.getAccountHolder() != null;

        if (hasAllAccountFields) {
            Account account = accountService.getAccount(user.getId());

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

            accountService.saveAccount(account);
        }
    }

    public void deleteUser(User user, HttpServletRequest request) {
        try {
            // 1. 사용자 비활성화
            user.setActive(false);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("사용자 비활성화 실패: {}", e.getMessage(), e);
        }

        try {
            // 2. 프로필 이미지 삭제
            storageUtil.deleteWithoutException(user.getProfileImage()); // 메서드 자체가 예외 없이 동작한다면 생략 가능
        } catch (Exception e) {
            log.warn("프로필 이미지 삭제 실패: {}", e.getMessage(), e);
        }
    }

    public void saveUser(User user){
        userRepository.save(user);
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
        pointService.savePoint(point);

        user.setPoint(currentPoint + pointValue);
        userRepository.save(user); // OptimisticLock 충돌 시 여기서 예외 발생
    }

    // 재시도 실패한 경우 fallback
    @Recover
    public void recover(OptimisticLockingFailureException e, PointEventType type, int pointValue, User user) {
        System.out.println("재시도 3회 실패: " + e.getMessage());
        throw new IllegalStateException("포인트 처리에 실패했습니다. 다시 시도해주세요.");
    }

    public int computeScore(int transactions, int reviews, double rating, int penalties) {
        int T = Math.max(0, transactions);
        int R = Math.max(0, Math.min(reviews, T));
        double r = clamp(rating, 0.0, 5.0);
        int P = Math.max(0, penalties);

        double fT = T_MAX * (1.0 - Math.exp(-T / K_T));
        double w = 1.0 - Math.exp(-R / K_R);
        double q = Math.tanh(2.0 * (r - 4.0));
        double fRate = RATE_MAX * w * q;
        double fR = R_MAX * (1.0 - Math.exp(-R / 100.0));
        double fP = -Math.min(PENALTY_CAP, PENALTY_UNIT * P);

        double raw = BASE + fT + fRate + fR + fP;
        int score = (int) Math.round(raw);

        return (int) clamp(score, SCORE_MIN, SCORE_MAX);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    // ====== 신뢰도 계산 후 저장 ======
    @Transactional
    public void updateUserTrustScore(User user,
                                     Integer transactions,
                                     Integer reviews,
                                     Double rating,
                                     Integer penalties) {
        ReviewAgg reviewAgg = reviewAggService.getReviewAgg(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("리뷰 정보가 존재하지 않습니다."));;

        // 값이 null이면 기존 값 유지
        int t = (transactions != null) ? transactions : user.getDealCount();
        int r = (reviews != null) ? reviews : reviewAgg.getReviewCount();
        double rate = (rating != null) ? rating : reviewAgg.getAvgRating();
        int p = (penalties != null) ? penalties : user.getPenaltyCount();
        log.info("t {}, r {}, rate {}, penalties {}", t, r, rate, p);

        // 신뢰도 계산
        int trustScore = computeScore(t, r, rate, p);

        // DB 저장
        user.setScore(trustScore);

        userRepository.save(user);
    }

    @Transactional
    public void penalize(User user){
        int penaltyCount = user.getPenaltyCount() + 1;
        user.setPenaltyCount(penaltyCount);
        updateUserTrustScore(user, null, null, null, penaltyCount);
        userRepository.save(user);
    }

    @Transactional
    public void addDealCount(String userId){
        User user = findById(userId);
        int dealCount = user.getDealCount() + 1;
        user.setDealCount(dealCount);
        updateUserTrustScore(user, dealCount, null, null, null);
        userRepository.save(user);
    }


    public ReviewAgg findReviewAggByUser(String userId) {
        return reviewAggRepository.findByUserId(userId)
                .orElse(ReviewAgg.builder().userId(userId).build());
    }
}
