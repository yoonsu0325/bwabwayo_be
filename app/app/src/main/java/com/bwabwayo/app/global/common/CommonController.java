package com.bwabwayo.app.global.common;

import com.bwabwayo.app.domain.product.domain.Courier;
import com.bwabwayo.app.domain.product.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class CommonController {

    private final CourierRepository courierRepository;

    @GetMapping("/courier")
    public ResponseEntity<?> getCourier(){
        List<Courier> courierList = courierRepository.findAll();
        return ResponseEntity.ok(courierList);
    }
}
