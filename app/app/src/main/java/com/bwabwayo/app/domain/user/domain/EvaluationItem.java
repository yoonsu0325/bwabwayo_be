package com.bwabwayo.app.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EvaluationItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;
}