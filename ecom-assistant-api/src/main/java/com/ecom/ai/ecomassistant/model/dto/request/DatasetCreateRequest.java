package com.ecom.ai.ecomassistant.model.dto.request;

import com.ecom.ai.ecomassistant.db.model.Permission;
import lombok.Data;

@Data
public class DatasetCreateRequest {
    private String name;
    private String description;
    private Permission permission;
}
