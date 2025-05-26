package com.ecom.ai.ecomassistant.db.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.repository.Collection;

import java.util.List;

@Getter
@Setter
@Builder
@Document
@Collection("User")
public class User extends AuditableDocument {
        @Id
        @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
        @Schema( description = "使用者ID", example = "user-001")
        private String id;

        @Schema( description = "使用者顯示名稱", example = "willy")
        private String name;

        @Schema( description = "使用者群組", example = """
                ["sales", "engineer"]""")
        private List<String> groups;

        @Schema( description = "使用者角色")
        private List<Role> roles;
}
