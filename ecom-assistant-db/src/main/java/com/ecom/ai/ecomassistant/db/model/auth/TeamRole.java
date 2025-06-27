package com.ecom.ai.ecomassistant.db.model.auth;

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
@Collection("team-role")
public class TeamRole {

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;

    private String teamId; // 若為 SYSTEM 則為 null

    private String name;

    private String description;

    private Set<String> permissions = new HashSet<>();

    private Boolean isSystemRole = false;
}
