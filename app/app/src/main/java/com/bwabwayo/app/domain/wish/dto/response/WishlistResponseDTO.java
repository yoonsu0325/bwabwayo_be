package com.bwabwayo.app.domain.wish.dto.response;

import com.bwabwayo.app.domain.wish.domain.Wish;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponseDTO {
    @Builder.Default
    private List<WishDTO> wishes = new ArrayList<>();

    private Integer current;
    private Integer start, last;
    private boolean prev, next;
    private Integer totalPages;
    private Long totalItems;
}
