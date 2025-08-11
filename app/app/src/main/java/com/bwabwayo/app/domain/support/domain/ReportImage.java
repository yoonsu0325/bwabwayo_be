package com.bwabwayo.app.domain.support.domain;

import com.bwabwayo.app.domain.support.controller.ReportController;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "report_image", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"report_id", "no"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
public class ReportImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    @Min(1)
    private int no; // 이미지 번호; 썸네일=1

    @Column(length = 2083, nullable = false)
    private String url; // 이미지 URL
}
