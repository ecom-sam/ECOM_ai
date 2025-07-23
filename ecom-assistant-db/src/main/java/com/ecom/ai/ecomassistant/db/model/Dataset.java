package com.ecom.ai.ecomassistant.db.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.repository.Collection;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Document
@Collection("dataset")
public class Dataset extends AuditableDocument {

    public enum AccessType {
        PUBLIC, PRIVATE, GROUP
    }

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;

    @NotNull(message = "Name cannot be null")
    private String name;

    private String description;

    private String teamId;

    private AccessType accessType = AccessType.GROUP;

    private Set<String> authorizedTeamIds = new HashSet<>();
}