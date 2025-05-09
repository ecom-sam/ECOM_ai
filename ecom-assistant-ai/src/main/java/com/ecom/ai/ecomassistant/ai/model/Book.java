package com.ecom.ai.ecomassistant.ai.model;

public record Book(
        int year,
        String category,
        String book,
        String review,
        String author,
        String summary
) {}