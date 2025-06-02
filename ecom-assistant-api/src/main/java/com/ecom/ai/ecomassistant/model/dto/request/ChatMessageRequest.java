package com.ecom.ai.ecomassistant.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChatMessageRequest(
        @NotBlank
        @Schema( description = "使用者問題", example = "請問如何執行...")
        String message,

        @NotNull
        @Schema( description = "是否執行RAG", example = "false")
        Boolean withRag,

        @Schema( description = "選擇的知識庫id", example = "[]")
        List<String> datasetIds
) {}
