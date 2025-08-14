package com.bwabwayo.app.domain.product.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 택배사 ID

    @Column(length = 20, nullable = false, unique = true)
    private String code; // 택배사 코드

    @Column(length = 255, nullable = false)
    private String name; // 택배사명

    // IE 브라우저 기준 URL 최대길이 = 2083
    @Column(name = "tracking_url", length = 2083)
    private String trackingUrl; // 택배사 URL
}
