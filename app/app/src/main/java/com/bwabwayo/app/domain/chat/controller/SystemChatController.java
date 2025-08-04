package com.bwabwayo.app.domain.chat.controller;

import com.bwabwayo.app.domain.chat.dto.request.SetInvoiceNumberRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetPriceRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetProductStatusRequest;
import com.bwabwayo.app.domain.chat.service.SystemChatService;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms/{roomId}")
public class SystemChatController {

    private final SystemChatService systemChatService;

    @PutMapping("/status")
    public ResponseEntity<?> setProductStatus(
            @PathVariable Long roomId,
            @RequestBody SetProductStatusRequest request){

        systemChatService.setProductStatus(roomId, request);
        return ResponseEntity.ok("판매 상태 변경이 완료되었습니다");
    }

    @PutMapping("/price")
    public ResponseEntity<?> setFinalPrice(
            @PathVariable Long roomId,
            @RequestBody SetPriceRequest request){

        SetProductStatusRequest productStatusRequest = SetProductStatusRequest.builder()
                .productStatus(SaleStatus.NEGOTIATING).build();

        systemChatService.setProductStatus(roomId, productStatusRequest);
        systemChatService.setFinalPrice(roomId, request);
        return ResponseEntity.ok("최종가격 설정이 완료되었습니다");
    }

    @PutMapping("/invoice")
    public ResponseEntity<?> setInvoiceNumber(
            @PathVariable Long roomId,
            @RequestBody SetInvoiceNumberRequest request){

        systemChatService.setInvoiceNumber(roomId, request);
        return ResponseEntity.ok("송장번호 입력이 완료되었습니다");
    }
}
