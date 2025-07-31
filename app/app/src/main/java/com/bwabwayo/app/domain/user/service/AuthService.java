package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.config.JwtProperties;
import com.bwabwayo.app.domain.user.domain.Account;
import com.bwabwayo.app.domain.user.domain.DeliveryAddress;
import com.bwabwayo.app.domain.user.domain.Role;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.dto.request.OAuth2UserRequest;
import com.bwabwayo.app.domain.user.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.dto.response.UserTokenResponse;
import com.bwabwayo.app.domain.user.repository.AccountRepository;
import com.bwabwayo.app.domain.user.repository.DeliveryAddressRepository;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.domain.user.utils.JWTUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final JWTUtils jwtUtils;
    private final JwtProperties jwtProperties;

    @Transactional
    public UserTokenResponse signUp(UserSignUpRequest request) {
        // 1. 필수 유저 정보 검증
        if (request.getId() == null || request.getNickname() == null) {
            throw new IllegalArgumentException("아이디 또는 닉네임이 누락되었습니다.");
        }
        Role role = Role.USER;
        OAuth2UserRequest oauth2 = new OAuth2UserRequest(request.getId(), role, "", "");
        String accessToken = jwtUtils.createToken(oauth2, jwtProperties.getAccessExpMinutes(), role);
        String refreshToken = jwtUtils.createToken(oauth2, jwtProperties.getRefreshExpMinutes(), role);

        // 2. User 저장
        User user = User.builder()
                .id(request.getId())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profileImage(request.getProfileImage())
                .bio("기본값")
                .score(500)
                .point(0)
                .dealCount(0)
                .penaltyCount(0)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .isActive(true)
                .role(role)
                .build();
        userRepository.save(user);

        // 3. Account 저장 조건 검사
        boolean hasCompleteAccountInfo =
                request.getAccountNumber() != null &&
                        request.getAccountHolder() != null &&
                        request.getBankName() != null;

        if (hasCompleteAccountInfo) {
            Account account = Account.builder()
                    .user(user)
                    .accountNumber(request.getAccountNumber())
                    .accountHolder(request.getAccountHolder())
                    .bankName(request.getBankName())
                    .build();
            accountRepository.save(account);
        }


        // 4. DeliveryAddress 저장 조건 검사
        boolean hasCompleteAddressInfo =
                request.getRecipientName() != null &&
                        request.getRecipientPhoneNumber() != null &&
                        request.getZipcode() != null &&
                        request.getAddress() != null &&
                        request.getAddressDetail() != null;

        if (hasCompleteAddressInfo) {
            DeliveryAddress address = DeliveryAddress.builder()
                    .user(user)
                    .recipientName(request.getRecipientName())
                    .recipientPhoneNumber(request.getRecipientPhoneNumber())
                    .zipcode(request.getZipcode())
                    .address(request.getAddress())
                    .addressDetail(request.getAddressDetail())
                    .build();
            deliveryAddressRepository.save(address);
        }

        // 5. 토큰 응답 반환
        return UserTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}

