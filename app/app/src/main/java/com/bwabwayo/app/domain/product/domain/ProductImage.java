package com.bwabwayo.app.domain.product.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "product_image", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "no"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품 이미지 ID

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 상품

    @Column(nullable = false)
    @Min(1)
    private int no; // 이미지 번호; 썸네일=1

    @Column(length = 2083, nullable = false)
    private String url; // 이미지 URL
}
