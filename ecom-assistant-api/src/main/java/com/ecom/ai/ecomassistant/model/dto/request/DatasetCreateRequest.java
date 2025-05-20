package com.ecom.ai.ecomassistant.model.dto.request;

import com.ecom.ai.ecomassistant.resource.Permission;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DatasetCreateRequest {
    @NotBlank
    private String name;
    private String description;
    private Permission permission;
}
