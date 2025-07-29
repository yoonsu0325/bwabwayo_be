package com.bwabwayo.app.domain.product.service;

import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.dto.request.ProductSearchRequestDTO;
import com.bwabwayo.app.domain.product.dto.response.ProductSearchResponseDTO;
import com.bwabwayo.app.domain.product.repository.ProductRepository;
import com.bwabwayo.app.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("상품 검색 - 기본 조건")
    void testSearchProducts() {
        // given
        ProductSearchRequestDTO requestDTO = ProductSearchRequestDTO.builder()
                .keyword("노트북")
                .categoryId(1L)
                .page(0)
                .size(5)
                .build();

        User seller = User.builder()
                .id("123")
                .nickname("홍길동")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("전자기기")
                .build();

        Product product = Product.builder()
                .id(1L)
                .title("맥북 프로")
                .category(category)
                .seller(seller)
                .price(2000000)
                .thumbnail("thumbnail.jpg")
                .build();


        List<Product> productList = List.of(product);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<Product> page = new PageImpl<>(productList, pageable, 1);

        when(productRepository.searchByCondition("노트북", 1L, pageable)).thenReturn(page);

        // when
        ProductSearchResponseDTO response = productService.searchProducts(requestDTO);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getResult()).hasSize(1);
        assertThat(response.getResult().get(0).getProduct().getTitle()).isEqualTo("맥북 프로");

        verify(productRepository).searchByCondition("노트북", 1L, pageable);
    }
}
