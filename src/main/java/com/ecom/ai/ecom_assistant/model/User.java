package com.ecom.ai.ecom_assistant.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.repository.Collection;

import java.util.List;

@Builder
@Collection("User")
public record User(
        @Id
        @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
        String id,
        String name,
        List<String> groups,
        List<Role> roles
) {
}
