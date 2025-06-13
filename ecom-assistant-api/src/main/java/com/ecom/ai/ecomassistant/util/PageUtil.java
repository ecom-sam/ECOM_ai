package com.ecom.ai.ecomassistant.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {
    public static Sort buildSort(String sortBy, String sortDir) {
        return "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }

    public static Pageable buildPageable(int page, int limit, String sortBy, String sortDir) {
        return PageRequest.of(page - 1, limit, buildSort(sortBy, sortDir));
    }
}

