package com.bwabwayo.app.domain.user.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailRequest {
    String nickname;
    String accountNumber;
    String bankName;
    String accountHolder;
    String profileImage;
    String email;
    String bio;
    String phoneNumber;
}
