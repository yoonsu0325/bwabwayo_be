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
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.service.WishService;
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

    @Value("${storage.path.temp}")
    private String tempPath;
    @Value("${storage.path.productImage}")
    private String productPath;


    /**
     * 상품 등록
     */
    @Transactional
    public Product createProduct(ProductCreateAndUpdateRequestDTO requestDTO, User user) {
        Product product = saveDTO(requestDTO, new Product(), user);
        productRepository.save(product);

        return product;
    }

    /**
     * 상품 검색
     */
    @Transactional(readOnly = true)
    public ProductSearchResponseDTO searchProducts(ProductSearchRequestDTO requestDTO, User loginUser) {
        String keyword = requestDTO.getKeyword();
        Long categoryId = requestDTO.getCategoryId();
        String sellerId = requestDTO.getSellerId();

        // 페이지는 1부터 시작
        Integer page = requestDTO.getPage();
        if(page == null || page < 1) page = 1;
        // 각 페이지에는 최소 0개가 할당
        Integer size = requestDTO.getSize();
        if(size == null || size < 0) size = 100;
        // 기본 정렬 속성은 '최신순'
        String sortBy = requestDTO.getSortBy();
        if(sortBy == null) sortBy = "latest";

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
                getSubCategoryIds(topCategory, categoryIds);
            } else {
                categoryIds.add(categoryId);
            }
        }
        
        // DB 조회
        Page<ProductWithWishDTO> pageData = productRepository.searchByCondition(keyword, categoryIds, pageable, loginUser != null ? loginUser.getId() : null, sellerId);
        List<ProductWithWishDTO> content = pageData.getContent();

        List<ProductSearchResultDTO> result = content.stream().map(dto -> {
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
        }).toList();

        int current = pageData.getNumber() + 1;
        int end = (int) Math.ceil(current / 10.0) * 10; // 마지막 페이지 블록
        int start = end - 9; // 처음 페이지 블록
        int last = Math.min(end, pageData.getTotalPages()); // 실제 마지막 페이지 블록

        return ProductSearchResponseDTO.builder()
                .size(result.size())
                .result(result)
                .start(start)
                .last(last)
                .prev(current > 1)
                .next(pageData.hasNext()) // end >= last
                .current(current)
                .totalPages(pageData.getTotalPages())
                .totalItems(pageData.getTotalElements())
                .build();
    }

    /**
     * 상품 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public ProductDetailResponseDTO getProductDetail(Product product, User user) {
        // 상품이 속한 카테고리부터 조상 카테고리까지의 모음
        List<CategoryDTO> superCategories = resolveSuperCategories(product.getCategory());

        // 상품에 포함된 이미지 URL 모음
        List<String> imageUrls = product.getProductImages().stream()
                .map(i -> storageService.getUrlFromKey(i.getUrl())).toList();
        List<String> imageKeys = product.getProductImages().stream()
                .map(ProductImage::getUrl).toList();
        
        // 판매자 정보
        User seller = product.getSeller();
        SellerDTO sellerDTO = SellerDTO.builder()
                .id(seller.getId())
                .nickname(seller.getNickname())
                .bio(seller.getBio())
                .profileImage(storageService.getUrlFromKey(seller.getProfileImage()))
                .score(seller.getScore())
                .rating(4.5) // TODO: 리뷰 통계와 연결 필요
                .build();

        return ProductDetailResponseDTO.builder()
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .saleStatus(product.getSaleStatus().getLevel())
                .canNegotiate(product.isCanNegotiate())
                .canDirect(product.isCanDirect())
                .canDelivery(product.isCanDelivery())
                .canVideoCall(product.isCanVideoCall())
                .isWish(user != null && wishService.existsWish(product.getId(), user.getId()))
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
        imageKeys.forEach(storageUtil::safeDelete);
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

    /**
     * 현재 카테고리와 모든 선조 카테고리의 모음 반환
     */
    private List<CategoryDTO> resolveSuperCategories(Category category) {
        List<CategoryDTO> result = new ArrayList<>();
        while (category != null) {
            result.add(new CategoryDTO(category.getId(), category.getName()));
            category = category.getParent();
        }
        Collections.reverse(result);
        return result;
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
        List<String> originImageKeys = dto.getImages();
        try {
            List<String> imageKeys = storageUtil.copyToPermanentDirectory(originImageKeys, productPath);
            
            // 이전 이미지 제거
            int oldImageCount = product.getProductImages() != null ? product.getProductImages().size() : 0;
            if(oldImageCount > 0){
                product.getProductImages().clear();
                productImageRepository.deleteAllByProduct(product);
                productImageRepository.flush();
            }

            setProductImages(product, imageKeys);

            // 이전 이미지 중 삭제되는 이미지를 스토리지에서 삭제
            if(oldImageCount > 0){
                Set<String> afterImages = new HashSet<>(imageKeys);
                for (String key : originImageKeys) {
                    if(!afterImages.contains(key)) {
                        storageUtil.safeDelete(key);
                    }
                }   
            }

            productRepository.save(product);
        } catch (Exception e) {
            // 상품 등록에 실패하면 영구 저장소로 복사한 이미지 롤백
            storageUtil.rollbackTemporalImages(originImageKeys, productPath);
            throw e;
        }

        return product;
    }

    private void setProductImages(Product product, List<String> imageKeys) {
        if (imageKeys != null && !imageKeys.isEmpty()) {
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
        } else{
            throw new NullPointerException("유효한 이미지가 존재하지 않습니다.");
        }
    }

}