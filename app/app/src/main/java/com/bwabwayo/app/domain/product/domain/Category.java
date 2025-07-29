package com.bwabwayo.app.domain.product.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"parent", "children"})
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 카테고리 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid")
    private Category parent; // 상위 카테고리

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children; // 하위 카테고리

    @Column(nullable = false, length = 255)
    private String name; // 카테고리 이름
}
