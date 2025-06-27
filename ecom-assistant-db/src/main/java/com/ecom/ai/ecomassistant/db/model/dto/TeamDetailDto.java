package com.ecom.ai.ecomassistant.db.model.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TeamDetailDto {
    private String id;
    private String name;
    private String description;

    private String ownerId;
    private String ownerName;

    private String createdBy;
    private String createdByDisplayName;
    private Instant createdDateTime;

    private String lastModifiedBy;
    private String lastModifiedByDisplayName;
    private Instant lastModifiedDateTime;
}
