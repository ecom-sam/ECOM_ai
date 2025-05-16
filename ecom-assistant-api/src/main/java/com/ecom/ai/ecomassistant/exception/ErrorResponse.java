package com.ecom.ai.ecomassistant.exception;

import java.util.List;

public record ErrorResponse<T>(
        int code,
        String message,
        List<T> errors
) {
}
