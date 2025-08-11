package com.bwabwayo.app.global.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private Integer size;
    @Builder.Default
    private List<T> result = new ArrayList<>();

    private Integer currentPage;
    private Integer startPage, lastPage;
    private Integer totalPages;
    private Long totalItems;

    private Boolean hasPrev, hasNext;

    public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
        int current = page.getNumber() + 1;
        int end = (int) Math.ceil(current / 10.0) * 10;
        int start = Math.max(1, end - 9);
        int last = Math.max(current, Math.min(end, page.getTotalPages()));

        List<R> mapped = page.getContent().stream()
                .map(mapper)
                .toList();

        return PageResponse.<R>builder()
                .size((int)page.getTotalElements())
                .result(mapped)
                .currentPage(current)
                .startPage(start)
                .lastPage(last)
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .hasPrev(current > 1)
                .hasNext(page.hasNext())
                .build();
    }

}
