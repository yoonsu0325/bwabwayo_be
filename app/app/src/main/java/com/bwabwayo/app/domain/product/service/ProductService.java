package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.chat.dto.request.SetInvoiceNumberRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetPriceRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetProductStatusRequest;
import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Courier;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.ProductImage;
import com.bwabwayo.app.domain.product.dto.request.ProductCreateAndUpdateRequestDTO;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.*;
import com.bwabwayo.app.domain.product.repository.CourierRepository;
import com.bwabwayo.app.domain.product.repository.ProductImageRepository;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.product.util.CategoryUtil;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.UserService;
import com.bwabwayo.app.domain.wish.service.WishService;
import com.bwabwayo.app.global.page.PageResponseDTO;
import com.bwabwayo.app.global.storage.util.StorageUtil;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final StorageService storageService;
    private final StorageUtil storageUtil;
    private final WishService wishService;
    private final CourierRepository courierRepository;
    private final ViewCountService viewCountService;
    private final UserService userService;
    private final ProductSimilarityService productSimilarityService;

    @Value("${storage.path.temp}")
    private String tempPath;
    @Value("${storage.path.productImage}")
    private String productPath;

    @Value("${product.detail.others}")
    private Integer otherCount;
    @Value("${product.detail.similarities}")
    private Integer similarityCount;


    /**
     * 상품 등록
     */
    @Transactional
    public Product createProduct(ProductCreateAndUpdateRequestDTO requestDTO, User user) {
        return saveDTO(requestDTO, new Product(), user);
    }

    /**
     * 상품 검색
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductSearchResultDTO> searchProducts(ProductSearchRequestDTO requestDTO, User loginUser) {
        String keyword = requestDTO.getKeyword();
        Long categoryId = requestDTO.getCategoryId();
        String sellerId = requestDTO.getSellerId();
        Boolean canVideoCall = requestDTO.getCanVideoCall();
        Boolean canNegotiate = requestDTO.getCanNegotiate();
        Boolean canDirect =  requestDTO.getCanDirect();
        Boolean canDelivery = requestDTO.getCanDelivery();
        Integer minPrice = requestDTO.getMinPrice();
        Integer maxPrice = requestDTO.getMaxPrice();

        // 페이지는 1부터 시작
        Integer page = requestDTO.getPage();
        // 각 페이지에는 최소 0개가 할당
        Integer size = requestDTO.getSize();
        // 기본 정렬 속성은 '최신순'
        String sortBy = requestDTO.getSortBy();

        // 정렬 조건 생성
        Sort.Order option = switch (sortBy){
            case "oldest" -> Sort.Order.asc("createdAt");
            case "views" -> Sort.Order.desc("view_count");
            case "wishes" -> Sort.Order.desc("wish_count");
            default -> Sort.Order.desc("createdAt");
        };
        Sort sort = Sort.by(option, Sort.Order.asc("id"));

        // 페이지네이션 생성
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 현재 카테고리에 포함되는 모든 카테고리의 모음 생성
        List<Long> categoryIds = new ArrayList<>();
        if(categoryId != null){
            if(categoryService.existsCategoryById(categoryId)) {
                Category topCategory = categoryService.getCategoryById(categoryId);
                categoryIds = CategoryUtil.getSubCategories(topCategory).stream().map(Category::getId).toList();
            } else {
                categoryIds.add(categoryId);
            }
        }
        
        // DB 조회
        Page<ProductWithWishDTO> pageData = productRepository.searchByCondition(
                keyword,
                categoryIds,
                pageable,
                loginUser != null ? loginUser.getId() : null,
                sellerId,
                canVideoCall,
                canNegotiate,
                canDelivery,
                canDirect,
                minPrice,
                maxPrice
        );

        return PageResponseDTO.fromEntity(pageData, dto -> {
            Product product = dto.getProduct();

            ProductSimpleDTO productDTO = ProductSimpleDTO.builder()
                    .id(product.getId())
                    .categoryId(product.getCategory().getId())
                    .thumbnail(storageService.getUrlFromKey(product.getThumbnail()))
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .viewCount(viewCountService.getViewCount(product.getId()).intValue())
                    .wishCount(product.getWishCount())
                    .chatCount(product.getChatCount())
                    .isLike(dto.getIsLike())
                    .canVideoCall(product.isCanVideoCall())
                    .saleStatusCode(product.getSaleStatus().getLevel())
                    .saleStatus(product.getSaleStatus().getDescription())
                    .createdAt(product.getCreatedAt())
                    .build();

            User seller = product.getSeller();
            UserSimpleDTO sellerDTO = new UserSimpleDTO(seller.getId(), seller.getNickname());

            return ProductSearchResultDTO.builder()
                    .product(productDTO)
                    .seller(sellerDTO)
                    .build();
        });
    }

    /**
     * 상품 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public ProductDetailResponseDTO getProductDetail(Product product, User loginUser) {
        // 상품이 속한 카테고리부터 조상 카테고리까지의 모음
        List<CategoryDTO> superCategories = CategoryUtil.getSupperCategories(product.getCategory())
                .stream().map(CategoryDTO::fromEntity).toList();

        // 상품에 포함된 이미지 URL 모음
        List<String> imageKeys = product.getProductImages().stream()
                .map(ProductImage::getUrl).toList();
        List<String> imageUrls = imageKeys.stream()
                .map(storageService::getUrlFromKey).toList();

        
        // 판매자 평균 평점 가져오기
        float avgRating = userService.getAvgRating(product.getSeller().getId());
        avgRating = Math.round(avgRating * 10.f) / 10.f;
        long reviewCount = userService.reviewCount(product.getSeller().getId());

        // 판매자 정보
        User seller = product.getSeller();
        List<ProductSimpleDTO> others = searchProducts(ProductSearchRequestDTO.builder().sellerId(seller.getId()).size(otherCount + 1).build(), loginUser)
                .getResult().stream()
                .map(ProductSearchResultDTO::getProduct)
                .filter(p ->!p.getId().equals(product.getId()))
                .limit(otherCount)
                .toList();

        SellerDTO sellerDTO = SellerDTO.builder()
                .id(seller.getId())
                .nickname(seller.getNickname())
                .bio(seller.getBio())
                .profileImage(storageService.getUrlFromKey(seller.getProfileImage()))
                .score(seller.getScore())
                .rating(avgRating)
                .reviewCount(reviewCount)
                .otherProducts(others)
                .build();

        // 유사한 상품 목록
        List<Long> similarities = productSimilarityService.searchSimilarTitles(product.getTitle(), similarityCount + 1);
        List<ProductSimpleDTO> productSimpleDTOS = similarities
                .stream()
                .filter(id-> !id.equals(product.getId()))
                .map(productRepository::getProductById)
                .filter(Objects::nonNull)
                .map(p-> ProductSimpleDTO.builder()
                        .id(p.getId())
                        .categoryId(p.getCategory().getId())
                        .thumbnail(storageService.getUrlFromKey(p.getThumbnail()))
                        .title(p.getTitle())
                        .price(p.getPrice())
                        .viewCount(viewCountService.getViewCount(p.getId()).intValue())
                        .wishCount(p.getWishCount())
                        .chatCount(p.getChatCount())
                        .isLike(loginUser != null && wishService.existsWish(p.getId(), loginUser.getId()))
                        .canVideoCall(p.isCanVideoCall())
                        .saleStatusCode(p.getSaleStatus().getLevel())
                        .saleStatus(p.getSaleStatus().getDescription())
                        .createdAt(p.getCreatedAt())
                        .build()
                ).toList();

        return ProductDetailResponseDTO.builder()
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .saleStatus(product.getSaleStatus().getLevel())
                .canNegotiate(product.isCanNegotiate())
                .canDirect(product.isCanDirect())
                .canDelivery(product.isCanDelivery())
                .canVideoCall(product.isCanVideoCall())
                .isLike(loginUser != null && wishService.existsWish(product.getId(), loginUser.getId()))
                .viewCount(viewCountService.getViewCount(product.getId()).intValue())
                .wishCount(product.getWishCount())
                .chatCount(product.getChatCount())
                .createdAt(product.getCreatedAt())
                .categories(superCategories)
                .imageUrls(imageUrls)
                .imageKeys(imageKeys)
                .seller(sellerDTO)
                .similarities(productSimpleDTOS)
                .build();
    }

    /**
     * 상품 정보 갱신
     */
    @Transactional
    public void update(Product product, ProductCreateAndUpdateRequestDTO requestDTO) {
        saveDTO(requestDTO, product, null);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void delete(Product product) {
        // 삭제할 이미지 URL 기록
        List<ProductImage> productImages = product.getProductImages();
        List<String> imageKeys = productImages.stream().map(ProductImage::getUrl).toList();

        // Product 삭제
        productRepository.delete(product);
        imageKeys.forEach(storageUtil::deleteWithoutException);
    }

    @Transactional
    public void setInvoiceNumber(SetInvoiceNumberRequest request, Long productId) {
        Product product = productRepository.getProductById(productId);
        product.setInvoiceNumber(request.getTrackingNumber());
        Courier courier = courierRepository.findByCode(request.getCourierCode());
        product.setCourier(courier);
    }

    @Transactional
    public void setPrice(SetPriceRequest request, Long productId) {
        Product product = productRepository.getProductById(productId);
        product.setPrice(request.getPrice());
    }
    @Transactional
    public void setStatus(SetProductStatusRequest request, Long productId) {
        Product product = productRepository.getProductById(productId);
        product.setSaleStatus(request.getProductStatus());
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));
    }

    /**
     * RequestDTO를 Entity로 변환 후 repository에 저장
     */
    private Product saveDTO(ProductCreateAndUpdateRequestDTO dto, Product product, User seller){
        Category category = categoryService.getCategoryById(dto.getCategoryId());
        if(category == null){
            throw new IllegalArgumentException("등록하려는 상품이 속한 카테고리가 존재하지 않습니다.");
        }

        // Product 속성 할당
        if(seller != null && product.getSeller() == null) {
            product.setSeller(seller);
        }
        product.setCategory(category);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setShippingFee(dto.getShippingFee());
        product.setCanNegotiate(dto.getCanNegotiate());
        product.setCanDirect(dto.getCanDirect());
        product.setCanDelivery(dto.getCanDelivery());
        product.setCanVideoCall(dto.getCanVideoCall());

        // 이미지 저장
        List<String> requestedImageKeys = dto.getImages();
        List<String> storedImageKeys;
        int oldImageCount;

        try {
            storedImageKeys = storageUtil.copyToDirectory(requestedImageKeys, tempPath, productPath);
            
            // 이전 이미지 제거
            List<ProductImage> productImages = product.getProductImages();
            oldImageCount = productImages != null ? productImages.size() : 0;
            if(oldImageCount > 0){
                product.getProductImages().clear();
                productImageRepository.deleteAllByProduct(product);
                productImageRepository.flush();
            }

            setProductImages(product, storedImageKeys);

            productRepository.save(product);
        } catch (Exception e) {
            // 상품 등록에 실패하면 영구 저장소로 복사한 이미지 롤백
            storageUtil.rollback(requestedImageKeys, tempPath, productPath);
            throw e;
        }

        // 이전 이미지 중 상품에 포함되지 않는 이미지를 삭제
        if(oldImageCount > 0){
            Set<String> updatedImages = new HashSet<>(storedImageKeys);
            for (String key : requestedImageKeys) {
                if(!updatedImages.contains(key)) {
                    storageUtil.deleteWithoutException(key);
                }
            }
        }

        return product;
    }

    private void setProductImages(Product product, List<String> imageKeys) {
        if(imageKeys == null || imageKeys.isEmpty()){
            throw new IllegalArgumentException("유효한 이미지가 존재하지 않습니다.");
        }

        int index = 0;
        for (String key : imageKeys) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .no(++index)
                    .url(key)
                    .build();
            product.getProductImages().add(image);
        }

        // 썸네일 지정
        product.setThumbnail(product.getProductImages().get(0).getUrl());
    }
}