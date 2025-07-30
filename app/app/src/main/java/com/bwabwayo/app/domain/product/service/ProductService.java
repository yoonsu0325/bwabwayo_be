package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.ProductImage;
import com.bwabwayo.app.domain.product.dto.request.ProductCreateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductCreateResponseDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.event.ProductDeletedEvent;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.repository.UserRepository;
import com.bwabwayo.app.global.s3.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    private final CategoryService categoryService;

    private final S3Service s3Service;

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    @Transactional
    public ProductCreateResponseDTO createProduct(ProductCreateRequestDTO requestDTO) {
        User seller = userRepository.findById("4371393546")
                .orElseThrow(() -> new EntityNotFoundException("판매자 정보를 찾을 수 없습니다."));
        Category category = categoryService.getCategoryById(requestDTO.getCategoryId());

        Product product = Product.builder()
                .category(category)
                .seller(seller)
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .price(requestDTO.getPrice())
                .shippingFee(requestDTO.getShippingFee())
                .canNegotiate(requestDTO.getCanNegotiate())
                .canDirect(requestDTO.getCanDirect())
                .canDelivery(requestDTO.getCanDelivery())
                .canVideoCall(requestDTO.getCanVideoCall())
                .build();

        List<String> imageKeys = requestDTO.getImages();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            int index = 1;
            for (String key : imageKeys) {
                if(index == 1) product.setThumbnail(key);

                ProductImage image = ProductImage.builder()
                        .product(product)
                        .no(index++)
                        .url(key)
                        .build();
                product.getProductImages().add(image);
            }
        }

        productRepository.save(product);

        return ProductCreateResponseDTO.builder().id(product.getId()).build();
    }


    /**
     * 상품 검색
     */
    public ProductSearchResponseDTO searchProducts(ProductSearchRequestDTO requestDTO) {
        String keyword = requestDTO.getKeyword();
        Long categoryId = requestDTO.getCategoryId();
        int page = requestDTO.getPage();
        int size = requestDTO.getSize();

        // 최신순 정렬; ID순 정렬
        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.asc("id")
        );
        
        // 페이지네이션
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 하위 카테고리 포함 카테고리 ID
        List<Long> categoryIds = new ArrayList<>();
        Category topCategory = categoryService.getCategoryById(categoryId);
        getSubCategoryIds(topCategory, categoryIds);
        
        // DB 조회
        Page<Product> pageData = productRepository.searchByCondition(keyword, categoryIds, pageable);
        
        // thumbnail을 URL로 확장W
        pageData.getContent().forEach(p-> p.setThumbnail(s3Service.getUrl(p.getThumbnail())));

        return ProductSearchResponseDTO.fromEntity(pageData);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long productId) {
        // 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 삭제할 이미지 URL 기록
        List<ProductImage> productImages = product.getProductImages();
        List<String> imageKeys = productImages.stream().map(ProductImage::getUrl).toList();

        // Product 삭제
        productRepository.delete(product);

        // 이벤트 발행
        eventPublisher.publishEvent(new ProductDeletedEvent(imageKeys));
    }

    
    /**
     * 현재 카테고리와 그 하위의 카테고리의 ID의 리스트 생성
     */
    private void getSubCategoryIds(Category category, List<Long> result){
        if(category == null) return;

        result.add(category.getId());
        for(Category subCategory : category.getChildren()){
            getSubCategoryIds(subCategory, result);
        }
    }
}