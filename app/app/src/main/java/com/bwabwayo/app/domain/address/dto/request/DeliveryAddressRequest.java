package com.bwabwayo.app.domain.address.dto.request;

import lombok.Getter;

@Getter
public class DeliveryAddressRequest {
    private String address;
    private String addressDetail;
    private String recipientName;
    private String recipientPhoneNumber;
    private String zipcode;
}
