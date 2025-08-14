package com.bwabwayo.app.domain.address.service;

import com.bwabwayo.app.domain.address.domain.DeliveryAddress;
import com.bwabwayo.app.domain.address.dto.request.DeliveryAddressRequest;
import com.bwabwayo.app.domain.address.dto.response.DeliveryAddressResponse;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.address.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    public void createAddressOnReSignup(User user, UserSignUpRequest request){
        DeliveryAddress address = buildAddress(user, request.getRecipientName(), request.getRecipientPhoneNumber(), request.getZipcode(), request.getAddress(), request.getAddressDetail());
        deliveryAddressRepository.save(address);
    }

    public void createAddressFromProfile(User user, DeliveryAddressRequest request){
        DeliveryAddress address = buildAddress(user, request.getRecipientName(), request.getRecipientPhoneNumber(), request.getZipcode(), request.getAddress(), request.getAddressDetail());
        deliveryAddressRepository.save(address);
    }

    private DeliveryAddress buildAddress(User user, String name, String phone, String zipcode, String addr, String detail) {
        return DeliveryAddress.builder()
                .user(user)
                .recipientName(name)
                .recipientPhoneNumber(phone)
                .zipcode(zipcode)
                .address(addr)
                .addressDetail(detail)
                .build();
    }

    public void updateAddressOnReSignup(User user, UserSignUpRequest request) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findTopByUser_Id(user.getId());
        applyAddressChanges(user, deliveryAddress, request.getRecipientName(),  request.getRecipientPhoneNumber(), request.getZipcode(), request.getAddress(), request.getAddressDetail());
    }

    public void updateAddressFromProfile(User user, DeliveryAddressRequest request, Long id){
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 존재하지 않습니다."));
        applyAddressChanges(user, deliveryAddress, request.getRecipientName(),  request.getRecipientPhoneNumber(), request.getZipcode(), request.getAddress(), request.getAddressDetail());
    }

    private void applyAddressChanges(User user, DeliveryAddress deliveryAddress, String name, String phone, String zipcode, String addr, String detail) {
        deliveryAddress.setAddress(addr);
        deliveryAddress.setZipcode(zipcode);
        deliveryAddress.setAddressDetail(detail);
        deliveryAddress.setRecipientName(name);
        deliveryAddress.setRecipientPhoneNumber(phone);
        deliveryAddressRepository.save(deliveryAddress);
    }

    public void deleteAddressFromProfile(User user, Long id){
        deliveryAddressRepository.deleteById(id);
    }

    public Page<DeliveryAddressResponse> getAll(User user, Pageable pageable) {
        Page<DeliveryAddress> deliveryAddressList = deliveryAddressRepository.findAllByUser_Id(user.getId(), pageable);
        return deliveryAddressList.map(address -> DeliveryAddressResponse.builder()
                .id(address.getId())
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .recipientName(address.getRecipientName())
                .recipientPhoneNumber(address.getRecipientPhoneNumber())
                .zipcode(address.getZipcode())
                .build());
    }


}
