package com.ecom.ai.ecomassistant.db.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema( description = "使用者ID", example = "user-001")
        String id,

        @Schema( description = "使用者顯示名稱", example = "willy")
        String name,

        @Schema( description = "使用者群組", example = """
                ["sales", "engineer"]""")
        List<String> groups,

        @Schema( description = "使用者角色")
        List<Role> roles
) {
}
