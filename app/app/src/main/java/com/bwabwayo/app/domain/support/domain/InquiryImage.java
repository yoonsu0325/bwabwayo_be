package com.bwabwayo.app.domain.support.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "inquiry_image", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"inquiry_id", "no"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
public class InquiryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(nullable = false)
    @Min(1)
    private int no; // 이미지 번호; 썸네일=1

    @Column(length = 2083, nullable = false)
    private String url; // 이미지 URL
}
