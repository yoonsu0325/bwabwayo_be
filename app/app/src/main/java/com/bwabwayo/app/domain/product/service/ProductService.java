package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.ProductImage;
import com.bwabwayo.app.domain.product.dto.request.ProductCreateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.*;
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
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    /**
     * 상품 등록
     */
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
    @Transactional
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
     * 상품 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public ProductDetailResponseDTO getProductDetail(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. id=" + id));

        // 상품이 속한 카테고리부터 조상 카테고리까지의 모음
        // 조상 카테고리가 먼저 저장됨
        List<CategoryDTO> superCategories = resolveSuperCategories(product.getCategory());

        // 상품에 포함된 이미지 URL 모음
        List<String> images = product.getProductImages().stream()
                .map(i -> s3Service.getUrl(i.getUrl())).toList();
        
        // 판매자 정보
        User seller = product.getSeller();
        SellerDTO sellerDTO = SellerDTO.builder()
                .id(seller.getId())
                .nickname(seller.getNickname())
                .profileImage(s3Service.getUrl(seller.getProfileImage()))
                .score(seller.getScore())
                .rating(4.5) // TODO: 리뷰 통계와 연결 필요
                .build();

        return ProductDetailResponseDTO.builder()
                .message("상품 상세 정보 조회에 성공하였습니다.")
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .saleStatus(product.getSaleStatus().getLevel())
                .canNegotiate(product.isCanNegotiate())
                .canDirect(product.isCanDirect())
                .canDelivery(product.isCanDelivery())
                .canVideoCall(product.isCanVideoCall())
                .isWish(false) // TODO: 위시리스트와 연결 필요
                .viewCount(product.getViewCount())
                .wishCount(product.getWishCount())
                .chatCount(product.getChatCount())
                .createdAt(product.getCreatedAt())
                .categories(superCategories)
                .images(images)
                .seller(sellerDTO)
                .build();
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProductById(Long productId) {
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

    private List<CategoryDTO> resolveSuperCategories(Category category) {
        List<CategoryDTO> result = new ArrayList<>();
        while (category != null) {
            result.add(new CategoryDTO(category.getId(), category.getName()));
            category = category.getParent();
        }
        Collections.reverse(result);
        return result;
    }
}