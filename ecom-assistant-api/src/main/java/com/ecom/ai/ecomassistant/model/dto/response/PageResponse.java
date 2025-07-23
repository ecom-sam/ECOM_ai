package com.ecom.ai.ecomassistant.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        List<T> data,
        PageInfo page
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), new PageInfo(page));
    }

    public record PageInfo(
            int currentPage,
            long totalItems,
            int totalPages,
            int limit
    ) {
        public PageInfo(Page<?> page) {
            this(
                    page.getNumber() + 1,
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.getSize()
            );
        }
    }
}
