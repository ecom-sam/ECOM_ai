package com.ecom.ai.ecom_assistant.model;

public record Book(
        int year,
        String category,
        String book,
        String review,
        String author,
        String summary
) {}