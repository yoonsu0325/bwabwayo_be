package com.bwabwayo.app.global.common;

import com.bwabwayo.app.domain.auth.annotation.LoginUser;
import com.bwabwayo.app.domain.chat.service.SystemChatService;
import com.bwabwayo.app.domain.product.domain.Courier;
import com.bwabwayo.app.domain.product.repository.CourierRepository;
import com.bwabwayo.app.domain.product.service.ProductService;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class CommonController {

    private final CourierRepository courierRepository;
    private final ProductService productService;
    private final SystemChatService systemChatService;

    @GetMapping("/courier")
    public ResponseEntity<?> getCourier(){
        List<Courier> courierList = courierRepository.findAll();
        return ResponseEntity.ok(courierList);
    }

    @PutMapping("/users/confirmation/{roomId}")
    public ResponseEntity<?> confirmPurchase(
            @LoginUser User user,
            @PathVariable Long roomId
    ){
        try{
            systemChatService.confirmPurchase(user, roomId);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("성공적으로 구매 확정이 되었습니다.");
    }
}
