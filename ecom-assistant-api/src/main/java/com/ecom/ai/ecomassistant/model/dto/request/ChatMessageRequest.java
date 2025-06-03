package com.ecom.ai.ecomassistant.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChatMessageRequest {
        @NotBlank
        @Schema( description = "使用者問題", example = "請問如何執行...")
        String message;

        @NotNull
        @Schema( description = "是否執行RAG", example = "false")
        Boolean withRag = false;

        @NotNull
        @Schema( description = "選擇的知識庫id", example = "[]")
        List<String> datasetIds = new ArrayList<>();
}
