package com.bwabwayo.app.domain.address.dto.response;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAddressResponse {
    private Long id;
    private String address;
    private String addressDetail;
    private String recipientName;
    private String recipientPhoneNumber;
    private String zipcode;
}
