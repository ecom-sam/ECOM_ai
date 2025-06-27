package com.ecom.ai.ecomassistant.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse<T>(
        int code,
        String message,
        List<T> errors
) {
}
