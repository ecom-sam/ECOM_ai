package com.ecom.ai.ecomassistant.model.dto.request;

import com.ecom.ai.ecomassistant.db.model.Dataset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DatasetCreateRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Dataset.AccessType accessType;
}
