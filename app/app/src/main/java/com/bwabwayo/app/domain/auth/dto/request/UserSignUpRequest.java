package com.bwabwayo.app.domain.auth.dto.request;

import lombok.Getter;

@Getter
public class UserSignUpRequest {
    // User
    private String id;
    private String nickname;
    private String email;
    private String phoneNumber;
    private String profileImage;

    // Account
    private String accountNumber;
    private String accountHolder;
    private String bankName;

    // DeliveryAddress
    private String recipientName;
    private String recipientPhoneNumber;
    private String zipcode;
    private String address;
    private String addressDetail;
}

