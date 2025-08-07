package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.chat.domain.ChatRoom;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.Sale;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.product.repository.SaleRepository;
import com.bwabwayo.app.domain.user.domain.PurchaseConfirmStatus;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.dto.response.UserOrderResponse;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final StorageService storageService;
    private final SaleRepository saleRepository;
    private final ProductService productService;

    public Sale startNegotiation(User user, ChatRoom chatRoom) {
        Product product = productService.findById(chatRoom.getProductId());
        Sale sale = Sale.builder()
                .product(product)
                .sellerId(chatRoom.getSellerId())
                .buyerId(chatRoom.getBuyerId())
                .salePrice(product.getPrice())
                .createdAt(LocalDateTime.now())
                .isReviewed(false)
                .roomId(chatRoom.getRoomId())
                .build();

        return saleRepository.save(sale);
    }

    public Page<UserOrderResponse> getOrders(String buyerId, Pageable pageable) {
        Page<Sale> sales = saleRepository.findWithProductAndCourierByBuyerId(buyerId, pageable);

        return sales.map(sale -> {
            Product product = sale.getProduct();

            String courierName = null;
            String trackingNumber = null;
            String deliveryStatus = null;

            if (product.getCourier() != null) {
                courierName = product.getCourier().getName();
                trackingNumber = product.getInvoiceNumber();
                deliveryStatus = product.getDeliveryStatus() != null
                        ? product.getDeliveryStatus().name()
                        : null;
            }

            int purchaseConfirmStatus = getPurchaseConfirmStatus(trackingNumber, product);

            return UserOrderResponse.builder()
                    .saleId(sale.getId())
                    .productId(product.getId())
                    .thumbnail(storageService.getUrlFromKey(product.getThumbnail()))
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .deliveryStatus(deliveryStatus)
                    .courierName(courierName)
                    .trackingNumber(trackingNumber)
                    .PurchaseConfirmStatus(purchaseConfirmStatus)
                    .build();
        });
    }

    private static int getPurchaseConfirmStatus(String trackingNumber, Product product) {
        int purchaseConfirmStatus = PurchaseConfirmStatus.IN_PROGRESS.getCode();
        if(trackingNumber != null){
            SaleStatus saleStatus = product.getSaleStatus();
            if(saleStatus == SaleStatus.NEGOTIATING){
                purchaseConfirmStatus = PurchaseConfirmStatus.ENABLED.getCode();
            } else if(saleStatus == SaleStatus.SOLD_OUT){
                purchaseConfirmStatus = PurchaseConfirmStatus.CONFIRMED.getCode();
            }
        }
        return purchaseConfirmStatus;
    }

    public Sale findByBuyerIdAndProductId(String buyerId, Long productId){
        List<Sale> sales = saleRepository.findByBuyerIdAndProductId(buyerId, productId);
        if(sales == null || sales.isEmpty()) throw new IllegalArgumentException("판매 내역이 존재하지 않습니다.");
        return sales.get(0);
    }

    @Transactional
    public void setPaid(Long saleId, boolean value){
        Sale sale = saleRepository.findById(saleId).orElseThrow(()->new IllegalArgumentException("판매 내역이 존재하지 않습니다."));
        sale.setPaid(value);
    }

    public Sale getSaleById(Long saleId){
        return saleRepository.findById(saleId).orElse(null);
    }

    public void saveSale(Sale sale){
        saleRepository.save(sale);
    }

}
