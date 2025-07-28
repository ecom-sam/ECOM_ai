package com.ecom.ai.ecomassistant.model.dto.request;

import com.ecom.ai.ecomassistant.db.model.Dataset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class DatasetCreateRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Dataset.AccessType accessType;
    
    @Size(max = 3, message = "最多只能設置 3 個 tags")
    private Set<String> tags = new HashSet<>();
}
