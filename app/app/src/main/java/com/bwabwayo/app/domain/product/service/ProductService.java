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
import com.bwabwayo.app.domain.product.exception.BadRequestException;
import com.bwabwayo.app.domain.product.exception.ForbiddenException;
import com.bwabwayo.app.domain.product.exception.NotFoundException;
import com.bwabwayo.app.domain.product.repository.CourierRepository;
import com.bwabwayo.app.domain.product.repository.ProductImageRepository;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.wish.service.WishService;
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
    private final WishService wishService;
    private final CourierRepository courierRepository;
    private final ViewCountService viewCountService;

    @Value("${storage.path.temp}")
    private String tempPath;
    @Value("${storage.path.productImage}")
    private String permanentPath;


    /**
     * 상품 등록
     */
    @Transactional
    public ProductCreateResponseDTO createProduct(ProductCreateAndUpdateRequestDTO requestDTO, User user) {
        Category category = categoryService.getCategoryById(requestDTO.getCategoryId());
        if(category == null){
            throw new BadRequestException("등록하려는 상품이 속한 카테고리가 존재하지 않습니다.");
        }
        
        // Product 생성
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

        // ProductImage 생성
        List<String> originImageKeys = requestDTO.getImages();

        try {
            List<String> imageKeys = copyToPermanentDirectory(originImageKeys);

            setProductImages(product, imageKeys);

            productRepository.save(product);
        } catch (Exception e) {
            // 상품 등록에 실패하면 영구 저장소로 복사한 이미지 롤백
            for(String key : originImageKeys){
                if(key.startsWith(tempPath)){
                    String target = permanentPath + key.substring(tempPath.length());
                    deleteImage(target);
                }
            }

            throw e;
        }

        return ProductCreateResponseDTO.builder()
                .id(product.getId())
                .build();
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
//            case "latest" -> Sort.Order.desc("createdAt");
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
    public ProductDetailResponseDTO getProductDetail(Long id, User user) {
        Product product = getProductById(id);
        if(product == null){
            throw new NotFoundException("상품이 존재하지 않습니다.");
        }

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
//                .profileImage(s3Service.getUrl(seller.getProfileImage()))
                .profileImage(seller.getProfileImage())
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
    public void updateProduct(Long productId, ProductCreateAndUpdateRequestDTO requestDTO, User user) {
        Product product = getProductById(productId);
        if(product == null) {
            throw new BadRequestException("수정하려는 상품이 존재하지 않습니다.");
        }

        if(!product.getSeller().getId().equals(user.getId())) {
            throw new ForbiddenException("자신이 등록한 상품만 수정할 수 있습니다.");
        }

        Category category = categoryService.getCategoryById(requestDTO.getCategoryId());
        if(category == null) {
            throw new BadRequestException("등록하려는 상품이 속한 카테고리가 존재하지 않습니다.");
        }

        product.setCategory(category);
        product.setTitle(requestDTO.getTitle());
        product.setDescription(requestDTO.getDescription());
        product.setPrice(requestDTO.getPrice());
        product.setShippingFee(requestDTO.getShippingFee());
        product.setCanNegotiate(requestDTO.getCanNegotiate());
        product.setCanDirect(requestDTO.getCanDirect());
        product.setCanDelivery(requestDTO.getCanDelivery());
        product.setCanVideoCall(requestDTO.getCanVideoCall());

        List<String> originImageKeys = requestDTO.getImages();
        try {
            List<String> imageKeys = copyToPermanentDirectory(originImageKeys);

            product.getProductImages().clear();
            productImageRepository.deleteAllByProduct(product);
            productImageRepository.flush();

            setProductImages(product, imageKeys);

            // 수정 후 사라지는 이미지 삭제
            Set<String> afterImages = new HashSet<>(imageKeys);
            for (String key : originImageKeys) {
                if(!afterImages.contains(key)) {
                    deleteImage(key);
                }
            }

            productRepository.save(product);
        } catch (Exception e) {
            // 상품 등록에 실패하면 영구 저장소로 복사한 이미지 롤백
            for(String key : originImageKeys){
                if(key.startsWith(tempPath)){
                    String target = permanentPath + key.substring(tempPath.length());
                    deleteImage(target);
                }
            }

            throw e;
        }
    }

    private void setProductImages(Product product, List<String> imageKeys) {
        if (!imageKeys.isEmpty()) {
            int index = 0;

            for (String key : imageKeys) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .no(++index)
                        .url(key)
                        .build();
                product.getProductImages().add(image);
            }
        }
        product.setThumbnail(product.getProductImages().get(0).getUrl());
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProductById(Long productId, User user) {
        // 상품이 존재하는지 확인
        Product product = getProductById(productId);

        // 상품을 삭제하려는 사용자가 상품의 판매자 또는 관리자인지 확인
        if(!product.getSeller().getId().equals(user.getId())) {
            throw new ForbiddenException("상품을 삭제할 권한이 없습니다: 자신이 등록한 상품만 삭제할 수 있습니다.");
        }

        // 삭제할 이미지 URL 기록
        List<ProductImage> productImages = product.getProductImages();
        List<String> imageKeys = productImages.stream().map(ProductImage::getUrl).toList();

        // Product 삭제
        productRepository.delete(product);

        imageKeys.forEach(this::deleteImage);
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

    public Product getProductById(Long id) {
        return productRepository.getProductById(id);
    }


    private List<String> copyToPermanentDirectory(List<String> images) {
        List<String> result = new ArrayList<>();

        for (String key : images) {
            if (!storageService.exists(key)) {
                throw new BadRequestException("스토리지에 존재하지 않는 이미지 입니다. key=" + key);
            }

            if (key.startsWith(tempPath)) {
                String target = permanentPath + key.substring(tempPath.length());

                storageService.copy(key, target);
                key = target;
            }
            result.add(key);
        }
        return result;
    }

    private void deleteImage(String imageKey) {
        try{
            storageService.delete(imageKey);
        } catch (Exception e){
            // 삭제 실패한 이미지는 로그로 남김
            log.warn("이미지 삭제 실패: file=" + imageKey, e);
        }
    }

    public void increaseWishCount(Long productId){
        productRepository.increaseWishCount(productId);
    }
    public void decreaseWishCount(Long productId){
        productRepository.decreaseWishCount(productId);
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
}