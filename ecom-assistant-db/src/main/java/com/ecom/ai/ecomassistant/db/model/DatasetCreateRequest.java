package com.ecom.ai.ecomassistant.db.model;

import lombok.Data;

@Data
public class DatasetCreateRequest {
    private String userId;

    private String name;
    private String description;
    private Permission permission;
}
