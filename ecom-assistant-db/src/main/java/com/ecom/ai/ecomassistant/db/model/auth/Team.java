package com.ecom.ai.ecomassistant.db.model.auth;

import com.ecom.ai.ecomassistant.db.model.AuditableDocument;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.repository.Collection;

@Getter
@Setter
@Document
@Collection("team")
public class Team extends AuditableDocument {

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;

    private String name;

    private String description;

    private String ownerUserId;

    private boolean active = true;
}
