package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.ProductImage;
import com.bwabwayo.app.domain.product.dto.ResponseMessage;
import com.bwabwayo.app.domain.product.dto.request.ProductCreateAndUpdateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.*;
import com.bwabwayo.app.domain.product.event.ProductDeletedEvent;
import com.bwabwayo.app.domain.product.exception.UnauthorizedProductAccessException;
import com.bwabwayo.app.domain.product.repository.ProductImageRepository;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.Role;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.global.s3.service.S3Service;
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

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryService categoryService;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * 상품 등록
     */
    @Transactional
    public ProductCreateResponseDTO createProduct(ProductCreateAndUpdateRequestDTO requestDTO, User user) {
        Category category = categoryService.getCategoryById(requestDTO.getCategoryId());

        Product product = Product.builder()
                .category(category)
                .seller(user)
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
        if(imageKeys == null) throw new IllegalArgumentException("유효한 이미지가 존재하지 않습니다.");
        productImageRepository.flush();
        int index = 0;
        for (String key : imageKeys) {
            if(!s3Service.exists(key)) {
                log.warn("등록하려는 이미지가 존재하지 않음: key={}", key);
                continue;
            }

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .no(++index)
                    .url(key)
                    .build();
            product.getProductImages().add(image);
        }
        if(index == 0) throw new IllegalArgumentException("유효한 이미지가 존재하지 않습니다.");
        product.setThumbnail(product.getProductImages().get(0).getUrl());

        productRepository.save(product);

        return ProductCreateResponseDTO.builder()
                .message(ResponseMessage.PRODUCT_CREATE_SUCCESS.getText())
                .id(product.getId())
                .build();
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
        if(categoryId != null){
            Category topCategory = categoryService.getCategoryById(categoryId);
            getSubCategoryIds(topCategory, categoryIds);
        }
        
        // DB 조회
        Page<Product> pageData = productRepository.searchByCondition(keyword, categoryIds, pageable);
        List<Product> content = pageData.getContent();

        List<ProductSearchResultDTO> result = content.stream().map(product -> {
            ProductSimpleDTO productDTO = ProductSimpleDTO.builder()
                    .id(product.getId())
                    .categoryId(product.getCategory().getId())
                    .thumbnail(s3Service.getUrl(product.getThumbnail()))
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .viewCount(product.getViewCount())
                    .wishCount(product.getWishCount())
                    .chatCount(product.getChatCount())
                    .isLike(false) // 위시 리스트 미구현
                    .canVideoCall(product.isCanVideoCall())
                    .saleStatusCode(product.getSaleStatus().getLevel())
                    .saleStatus(product.getSaleStatus().getDescription())
                    .createdAt(product.getCreatedAt())
                    .build();

            User user = product.getSeller();
            UserSimpleDTO userDTO = new UserSimpleDTO(user.getId(), user.getNickname());

            return ProductSearchResultDTO.builder()
                    .product(productDTO)
                    .seller(userDTO)
                    .build();
        }).toList();

        int current = pageData.getNumber() + 1;
        int end = (int) Math.ceil(current / 10.0) * 10; // 마지막 페이지 블록
        int start = end - 9; // 처음 페이지 블록
        int last = Math.min(end, pageData.getTotalPages()); // 실제 마지막 페이지 블록


        return ProductSearchResponseDTO.builder()
                .message(ResponseMessage.PRODUCT_SEARCH_SUCCESS.getText())
                .result(result)
                .start(start)
                .last(last)
                .prev(current > 1)
                .next(pageData.hasNext())
                .current(current)
                .totalPages(pageData.getTotalPages())
                .totalItems(pageData.getTotalElements())
                .build();
    }

    /**
     * 상품 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public ProductDetailResponseDTO getProductDetail(Long id) {
        Product product = getProductById(id);

        // 상품이 속한 카테고리부터 조상 카테고리까지의 모음
        // 조상 카테고리가 먼저 저장됨
        List<CategoryDTO> superCategories = resolveSuperCategories(product.getCategory());

        // 상품에 포함된 이미지 URL 모음
        List<String> imageUrls = product.getProductImages().stream()
                .map(i -> s3Service.getUrl(i.getUrl())).toList();
        List<String> imageKeys = product.getProductImages().stream()
                .map(ProductImage::getUrl).toList();
        
        // 판매자 정보
        User seller = product.getSeller();
        SellerDTO sellerDTO = SellerDTO.builder()
                .id(seller.getId())
                .nickname(seller.getNickname())
//                .profileImage(s3Service.getUrl(seller.getProfileImage()))
                .profileImage(seller.getProfileImage())
                .score(seller.getScore())
                .rating(4.5) // TODO: 리뷰 통계와 연결 필요
                .build();

        return ProductDetailResponseDTO.builder()
                .message(ResponseMessage.PRODUCT_DETAIL_SUCCESS.getText())
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
                .imageUrls(imageUrls)
                .imageKeys(imageKeys)
                .seller(sellerDTO)
                .build();
    }

    /**
     * 상품 정보 갱신
     */
    @Transactional
    public void updateProduct(Long productId, ProductCreateAndUpdateRequestDTO requestDTO) {
        Product product = getProductById(productId);
        Category category = categoryService.getCategoryById(requestDTO.getCategoryId());

        product.setCategory(category);
        product.setTitle(requestDTO.getTitle());
        product.setDescription(requestDTO.getDescription());
        product.setPrice(requestDTO.getPrice());
        product.setShippingFee(requestDTO.getShippingFee());
        product.setCanNegotiate(requestDTO.getCanNegotiate());
        product.setCanDirect(requestDTO.getCanDirect());
        product.setCanDelivery(requestDTO.getCanDelivery());
        product.setCanVideoCall(requestDTO.getCanVideoCall());
        
        
        Set<String> orphanImages = new HashSet<>();
        for(ProductImage image : product.getProductImages()){
            orphanImages.add(image.getUrl());
        }

        List<String> imageKeys = requestDTO.getImages();
        if (imageKeys == null) throw new IllegalArgumentException("유효한 이미지가 존재하지 않습니다.");

        product.getProductImages().clear();
        productImageRepository.deleteAllByProduct(product);

        int index = 0;
        for (String key : imageKeys) {
            if(!s3Service.exists(key)) {
                log.warn("등록하려는 이미지가 존재하지 않음: key={}", key);
                continue;
            }

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .no(++index)
                    .url(key)
                    .build();
            product.getProductImages().add(image);
            orphanImages.remove(key);
        }
        if(index == 0) throw new IllegalArgumentException("유효한 이미지가 존재하지 않습니다.");
        product.setThumbnail(product.getProductImages().get(0).getUrl());

        // 이미지 삭제
        for(String key : orphanImages){
            s3Service.deleteFile(key);
        }
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProductById(Long productId, User user) {
        // 상품이 존재하는지 확인
        Product product = getProductById(productId);

        // 상품을 삭제하려는 사용자가 상품의 판매자 또는 관리자인지 확인
        if(user.getRole() == Role.ADMIN ||
                !product.getSeller().getId().equals(user.getId())) {
            throw new UnauthorizedProductAccessException("상품을 삭제할 권한이 없습니다: 자신이 등록한 상품만 삭제할 수 있습니다.");
        }

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

    public Product getProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. id=" + id));
    }
}