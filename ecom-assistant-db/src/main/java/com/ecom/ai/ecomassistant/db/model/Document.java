package com.ecom.ai.ecomassistant.db.model;

import com.ecom.ai.ecomassistant.common.resource.StorageType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.repository.Collection;

@Getter
@Setter
@Builder
@Collection("document")
public class Document extends AuditableDocument {
    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;

    @NotBlank
    private String datasetId;

    private String fileName;

    private String fullPath;

    private StorageType storageType;
}