package com.bwabwayo.app.domain.product.repository;

import com.bwabwayo.app.domain.global.config.QuerydslConfig;
import com.bwabwayo.app.domain.product.domain.Category;
import com.bwabwayo.app.domain.product.domain.Product;
import com.bwabwayo.app.domain.product.enums.DeliveryStatus;
import com.bwabwayo.app.domain.product.enums.SaleStatus;
import com.bwabwayo.app.domain.user.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class ProductRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(ProductRepositoryTest.class);
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager em;

    private Category category1;
    private Category category2;
    private User seller;

    @BeforeEach
    void setup() {
        // Category
        category1 = Category.builder().id(1L).name("전자기기").build();
        category2 = Category.builder().id(2L).name("도서").build();
        em.persist(category1);
        em.persist(category2);

        // User
        seller = User.builder().id("123").nickname("판매자1").build();
        em.persist(seller);

        // Product
        Product product1 = Product.builder()
                .title("아이폰 13 미개봉")
                .description("미개봉 새상품입니다.")
                .category(category1)
                .seller(seller)
                .price(1000000)
                .thumbnail("https://example.com/image1.jpg")
                .saleStatus(SaleStatus.AVAILABLE)
                .deliveryStatus(DeliveryStatus.RECEIVED)
                .canDelivery(true)
                .canDirect(true)
                .canNegotiate(true)
                .canVideoCall(false)
                .build();

        Product product2 = Product.builder()
                .title("갤럭시 S23 울트라")
                .description("삼성 갤럭시 S23 울트라 블랙, 개봉 후 미사용")
                .category(category1) // 전자기기
                .seller(seller)
                .price(1200000)
                .thumbnail("https://example.com/galaxy.jpg")
                .saleStatus(SaleStatus.AVAILABLE)
                .deliveryStatus(DeliveryStatus.OUT_FOR_DELIVERY)
                .canDelivery(true)
                .canDirect(false)
                .canNegotiate(true)
                .canVideoCall(true)
                .build();

        Product product3 = Product.builder()
                .title("자바의 정석 3판")
                .description("깨끗하게 사용한 자바의 정석 책")
                .category(category2) // 도서
                .seller(seller)
                .price(25000)
                .thumbnail("https://example.com/java.jpg")
                .saleStatus(SaleStatus.SOLD_OUT)
                .deliveryStatus(DeliveryStatus.DIRECT)
                .canDelivery(true)
                .canDirect(true)
                .canNegotiate(false)
                .canVideoCall(false)
                .build();

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
    }

    @Test
    @DisplayName("상품 검색 - 키워드 검색")
    void searchByCondition_keyword() {
        // given
        String keyword = "아이폰";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());

        // when
        Page<Product> result = productRepository.searchByCondition(keyword, null, pageable);

        // then
        List<Product> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("title")
                .contains("아이폰 13 미개봉");
    }

    @Test
    @DisplayName("상품 검색 - 카테고리 검색")
    void searchByCondition_category() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());

        // when
        Page<Product> result = productRepository.searchByCondition(null, category1.getId(), pageable);

        // then
        List<Product> content = result.getContent();
        assertThat(content).hasSize(2);
    }

    @Test
    @DisplayName("상품 검색 - 제목 및 카테고리 검색")
    void searchByCondition_both() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());

        // when
        Page<Product> result = productRepository.searchByCondition("갤럭시", category1.getId(), pageable);

        // then
        List<Product> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("title")
                .contains("갤럭시 S23 울트라");
    }

    @Test
    @DisplayName("상품 검색 - 낮은가격순")
    void searchByCondition_sort() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").ascending());

        // when
        Page<Product> result = productRepository.searchByCondition(null, null, pageable);

        // then
        List<Product> content = result.getContent();
        assertThat(content.get(0)).extracting("title").isEqualTo("자바의 정석 3판");
    }


    @Test
    @DisplayName("상품 검색 - 검색 결과 없음")
    void searchByCondition_noResult() {
        // when
        Page<Product> result = productRepository.searchByCondition("없는 키워드", null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        result.getContent().forEach(e->log.debug(e.toString()));

    }
}
