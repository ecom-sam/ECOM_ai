package com.ecom.ai.ecomassistant.db.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.repository.Collection;

@Data
@Collection("Testing")
public class Dataset {

    @Id
    private String id;

    private String userId;

    private String name;
    private String description;
    private String permission;
}