package com.ecom.ai.ecomassistant.exception;


public record ValidationError(
        String field,
        String message
) {
}
