package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.ai.dto.response.QueryItemDto;
import com.bwabwayo.app.domain.ai.service.ProductEmbeddingService;
import com.bwabwayo.app.domain.chat.dto.request.SetInvoiceNumberRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetPriceRequest;
import com.bwabwayo.app.domain.chat.dto.request.SetProductStatusRequest;
import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Courier;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.domain.ProductImage;
import com.bwabwayo.app.domain.product.dto.ProductQueryCondition;
import com.bwabwayo.app.domain.product.dto.request.ProductUpsertRequest;
import com.bwabwayo.app.domain.product.dto.request.ProductQueryRequest;
import com.bwabwayo.app.domain.product.dto.response.*;
import com.bwabwayo.app.domain.product.enums.DeliveryStatus;
import com.bwabwayo.app.domain.product.enums.ProductSortType;
import com.bwabwayo.app.domain.product.exception.ProductNotFoundException;
import com.bwabwayo.app.domain.product.repository.CategoryRepository;
import com.bwabwayo.app.domain.product.repository.CourierRepository;
import com.bwabwayo.app.domain.product.repository.ProductImageRepository;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.product.util.CategoryUtils;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.user.service.ReviewAggService;
import com.bwabwayo.app.domain.wish.service.WishService;
import com.bwabwayo.app.global.page.PageResponse;
import com.bwabwayo.app.global.storage.util.StorageUtil;
import com.bwabwayo.app.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final ProductEmbeddingService productEmbeddingService;
    private final ReviewAggService reviewAggService;
    private final CategoryRepository categoryRepository;

    @Value("${storage.path.temp}")
    private String tempPath;
    @Value("${storage.path.productImage}")
    private String productPath;

    private final Integer OTHER_COUNT = 5;


    /**
     * 상품 등록
     */
    @Transactional
    public Product createProduct(ProductUpsertRequest requestDTO, User user) {
        return upsert(requestDTO, new Product(), user);
    }

    /**
     * 상품 정보 갱신
     */
    @Transactional
    public void update(Product product, ProductUpsertRequest requestDTO) {
        upsert(requestDTO, product, product.getSeller());
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void delete(Product product) {
        wishService.deleteAllByProduct(product);

        // 삭제할 이미지 URL 기록
        List<ProductImage> productImages = product.getProductImages();
        List<String> imageKeys = productImages.stream().map(ProductImage::getUrl).toList();

        // Product 삭제
        productRepository.delete(product);
        imageKeys.forEach(storageUtil::deleteWithoutException);
    }

    /**
     * 상품 검색
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductQueryResult> query(ProductQueryRequest requestDTO, User loginUser) {
        // 검색 조건
        String keyword = requestDTO.getKeyword();
        Long productId = requestDTO.getProductId();
        if(keyword == null & productId != null){
            keyword = productRepository.getProductById(productId).getTitle();
        }
        Long categoryId = requestDTO.getCategoryId();
        String sellerId = requestDTO.getSellerId();

        Boolean canVideoCall = requestDTO.getCanVideoCall();
        Boolean canNegotiate = requestDTO.getCanNegotiate();
        Boolean canDirect =  requestDTO.getCanDirect();
        Boolean canDelivery = requestDTO.getCanDelivery();

        Integer minPrice = requestDTO.getMinPrice();
        Integer maxPrice = requestDTO.getMaxPrice();
        
        // 현재 카테고리에 포함되는 모든 카테고리의 모음 생성
        List<Long> categoryIds = null;
        if(categoryId != null){
            Category topCategory = categoryService.findByIdOptional(categoryId).orElse(null);
            categoryIds = CategoryUtils.getSubCategories(topCategory).stream().map(Category::getId).toList();

            // fallback
            if(categoryIds.isEmpty()) {
                categoryIds = List.of(categoryId);
            }
        }

        categoryIds = keywordToCategory(keyword, categoryIds);

        // 페이징 조건
        // 페이지는 1부터 시작
        Integer page = requestDTO.getPage();
        // 각 페이지에는 최소 0개가 할당
        Integer size = requestDTO.getSize();
        // 기본 정렬 속성은 '최신순'
        ProductSortType sortType = ProductSortType.from(requestDTO.getSortBy());
        if((sortType == ProductSortType.RELATED || sortType == ProductSortType.LATEST_AND_RELATED)
                && (keyword == null || keyword.isBlank())){ // 키워드가 없으면 관련 검색 불가
            log.warn("키워드가 없어 관련성 검색이 불가합니다; 기본 검색으로 대체");
            sortType = ProductSortType.LATEST;
        }

        // 페이지네이션 생성
        Pageable pageable = PageRequest.of(page - 1, size, sortType.getSort());
        
        // DB 조회
        ProductQueryCondition queryCondition = ProductQueryCondition.builder()
                .viewerId(loginUser != null ? loginUser.getId() : null)
                .keyword(keyword)
                .categoryIn(categoryIds)
                .sellerId(sellerId)
                .canVideoCall(canVideoCall)
                .canNegotiate(canNegotiate)
                .canDirect(canDirect)
                .canDelivery(canDelivery)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
//                .urlPrefix(requestDTO.getUrlPrefix())
                .getOnlySale(requestDTO.getOnlySale())
                .build();

        Page<ProductWithIsLikeDTO> pageData;
        if(sortType == ProductSortType.RELATED) {
            pageData = queryWithRelated(queryCondition, pageable, loginUser);
        } else if(sortType != ProductSortType.LATEST_AND_RELATED){
            pageData = productRepository.searchByCondition(queryCondition, pageable);
        } else{
            Page<ProductWithIsLikeDTO> related = queryWithRelated(queryCondition, pageable, loginUser);
            Page<ProductWithIsLikeDTO> latest = productRepository.searchByCondition(queryCondition, pageable);
            pageData = merge(related, latest, pageable.getPageSize());
        }

        return PageResponse.from(pageData, dto -> {
            Product product = dto.getProduct();

            ProductDTO productDTO = ProductDTO.builder()
                    .isMine(product.getSeller().equals(loginUser))
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
            SellerDTO sellerDTO = new SellerDTO(seller.getId(), seller.getNickname());

            return ProductQueryResult.builder()
                    .product(productDTO)
                    .seller(sellerDTO)
                    .build();
        });
    }

    private List<Long> keywordToCategory(String keyword, List<Long> categoryIds) {
        return categoryIds;
//        if(keyword == null) return categoryIds;
//
//        if(categoryIds == null) categoryIds = new ArrayList<>();
//        String[] tokens = keyword.split(" ");
//        for(String token : tokens) {
//            for (Category category : categoryRepository.findAll()) {
//                if (category.getName().contains(token) || token.contains(category.getName())) {
//                    List<Long> temp = CategoryUtils.getSubCategories(category).stream().map(Category::getId).toList();
//                    categoryIds.addAll(temp);
//                }
//            }
//        }
//        log.info("ctegorys={}", Arrays.toString(categoryIds.toArray()));
//        return categoryIds;
    }

    private Page<ProductWithIsLikeDTO> queryWithRelated(ProductQueryCondition queryCondition, Pageable pageable, User viewer){
        List<QueryItemDto> query = productEmbeddingService.query(queryCondition, pageable);
        query = query.stream().filter(dto->dto.getScore() > 0.3).toList();

        List<Long> ids = query.stream().map(QueryItemDto::getId).toList();
        if (ids.isEmpty()) return Page.empty(pageable);

        List<ProductWithIsLikeDTO> products = productRepository.findByIdsInOrder(ids, viewer != null ? viewer.getId() : null);

        return new PageImpl<>(products, pageable, productRepository.getCount(queryCondition));
    }

    public List<Product> recommendTopK(String keyword, int k){
        ProductQueryCondition condition = ProductQueryCondition.builder()
                .keyword(keyword)
                .build();
        Pageable pageable = PageRequest.of(0, k);
        return queryWithRelated(condition, pageable, null).map(ProductWithIsLikeDTO::getProduct).toList();
    }

    /**
     * 상품 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Product product, User loginUser) {
        // 상품이 속한 카테고리부터 조상 카테고리까지의 모음
        List<CategoryDTO> superCategories = CategoryUtils.getSupperCategories(product.getCategory())
                .stream().map(CategoryDTO::from).toList();

        // 상품에 포함된 이미지 URL 모음
        List<String> imageKeys = product.getProductImages().stream()
                .map(ProductImage::getUrl).toList();
        List<String> imageUrls = imageKeys.stream()
                .map(storageService::getUrlFromKey).toList();

        
        // 판매자 평균 평점 가져오기
        float avgRating = reviewAggService.getAvgRating(product.getSeller().getId());
        avgRating = Math.round(avgRating * 10.f) / 10.f;
        long reviewCount = reviewAggService.getReviewCount(product.getSeller().getId());

        // 판매자 정보
        User seller = product.getSeller();
        List<ProductDTO> others = query(ProductQueryRequest.builder().sellerId(seller.getId()).size(OTHER_COUNT + 1).urlPrefix("http").build(), loginUser)
                .getResult().stream()
                .map(ProductQueryResult::getProduct)
                .filter(p ->!p.getId().equals(product.getId()))
                .limit(OTHER_COUNT)
                .toList();

        SellerDetailDTO sellerDTO = SellerDetailDTO.builder()
                .id(seller.getId())
                .nickname(seller.getNickname())
                .bio(seller.getBio())
                .profileImage(storageService.getUrlFromKey(seller.getProfileImage()))
                .score(seller.getScore())
                .rating(avgRating)
                .reviewCount(reviewCount)
                .otherProducts(others)
                .build();

        return ProductDetailResponse.builder()
                .isMine(product.getSeller().equals(loginUser))
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .saleStatus(product.getSaleStatus().getLevel())
                .shippingFee(product.getShippingFee())
                .canNegotiate(product.isCanNegotiate())
                .canDirect(product.isCanDirect())
                .canDelivery(product.isCanDelivery())
                .canVideoCall(product.isCanVideoCall())
                .isLike(loginUser != null && wishService.existsWish(product, loginUser))
                .viewCount(viewCountService.getViewCount(product.getId()).intValue())
                .wishCount(product.getWishCount())
                .chatCount(product.getChatCount())
                .createdAt(product.getCreatedAt())
                .categories(superCategories)
                .imageUrls(imageUrls)
                .imageKeys(imageKeys)
                .seller(sellerDTO)
                .build();
    }

    @Transactional
    public void setInvoiceNumber(SetInvoiceNumberRequest request, Long productId) {
        Product product = productRepository.getProductById(productId);
        product.setInvoiceNumber(request.getTrackingNumber());
        Courier courier = courierRepository.findByCode(request.getCourierCode());
        product.setCourier(courier);
        if(product.getDeliveryStatus() == null){
            product.setDeliveryStatus(DeliveryStatus.PREPARING);
        }
    }

    @Transactional
    public void setPrice(SetPriceRequest request, Long productId) {
        Product product = productRepository.getProductById(productId);
        product.setPrice(request.getPrice());
    }
    @Transactional
    public void setStatus(SetProductStatusRequest request, Long productId) {
        Product product = productRepository.getProductById(productId);
        System.out.println(productId +" " + product.getSaleStatus());
        product.setSaleStatus(request.getProductStatus());
        System.out.println(product.getSaleStatus());
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Product upsert(ProductUpsertRequest dto, Product product, User seller){
        if(seller != null && product.getSeller() == null)  product.setSeller(seller);

        Category category = categoryService.findById(dto.getCategoryId());
        while(!category.getChildren().isEmpty()){ // 상품이 말단 카테고리에 소속되도록 강제
            category = category.getChildren().get(0);
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

    public List<Product> getDeliveringProductsByDeliveryStatus(DeliveryStatus status){
        return productRepository.findAllByDeliveryStatus(status);
    }

    public List<Product> getWillDeliveryProducts(){
        return productRepository.findAllByEmptyInvoiceNumberAndDeliveryStatus(DeliveryStatus.PREPARING);
    }

    private Page<ProductWithIsLikeDTO> merge(Page<ProductWithIsLikeDTO> related, Page<ProductWithIsLikeDTO> latest, int limit){
        List<ProductWithIsLikeDTO> list = new ArrayList<>(latest.getContent());

        if(latest.getTotalElements() < limit){
            Set<Long> set = new HashSet<>();
            for(ProductWithIsLikeDTO r : latest.getContent()) set.add(r.getProduct().getId());

            if(list.size() < limit) {
                for(ProductWithIsLikeDTO r : related.getContent()){
                    if(!set.contains(r.getProduct().getId())) {
                        list.add(r);
                        if(list.size() == limit) break;
                    }
                }
            }
        }
        return new PageImpl<>(list);
    }
}