package com.ecom.ai.ecomassistant.db.model.auth;

import com.ecom.ai.ecomassistant.db.model.AuditableDocument;
import lombok.Builder;
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
@Builder
@Document
@Collection("team-membership")
public class TeamMembership extends AuditableDocument {

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;

    private String userId;

    private String teamId;

    @Builder.Default
    private Set<String> teamRoles = new HashSet<>();

}
