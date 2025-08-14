package com.bwabwayo.app.domain.address.controller;

import com.bwabwayo.app.domain.address.dto.request.DeliveryAddressRequest;
import com.bwabwayo.app.domain.address.dto.response.DeliveryAddressResponse;
import com.bwabwayo.app.domain.address.service.DeliveryAddressService;
import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/addresses")
public class AddressController {
    private final DeliveryAddressService deliveryAddressService;

    @GetMapping
    public ResponseEntity<?> getAll(@LoginUser User user,
            @PageableDefault(size = 5) Pageable pageable) {
        Page<DeliveryAddressResponse> deliveryAddressPage = deliveryAddressService.getAll(user, pageable);
        return ResponseEntity.ok(deliveryAddressPage);
    }

    @PostMapping
    public ResponseEntity<?> createAddress(@LoginUser User user, @RequestBody DeliveryAddressRequest request){
        deliveryAddressService.createAddressFromProfile(user, request);
        return ResponseEntity.ok("배송지 등록 완료");
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@LoginUser User user, @PathVariable("addressId") Long id, @RequestBody DeliveryAddressRequest request){
        deliveryAddressService.updateAddressFromProfile(user, request, id);
        return ResponseEntity.ok("배송지 수정 완료");
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@LoginUser User user, @PathVariable("addressId") Long id){
        deliveryAddressService.deleteAddressFromProfile(user, id);
        return ResponseEntity.ok("배송지 삭제 완료");
    }

}
