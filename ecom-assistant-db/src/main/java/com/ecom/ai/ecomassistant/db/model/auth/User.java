package com.ecom.ai.ecomassistant.db.model.auth;

import com.ecom.ai.ecomassistant.common.UserStatus;
import com.ecom.ai.ecomassistant.db.model.AuditableDocument;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Collection("user")
public class User extends AuditableDocument {
        @Id
        @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
        @Schema( description = "使用者ID", example = "user-001")
        private String id;

        @Schema( description = "使用者顯示名稱", example = "willy")
        private String name;

        private String email;

        private String password;

        @Builder.Default
        private UserStatus status = UserStatus.INVITED;

        @Builder.Default
        private Set<String> systemRoles = new HashSet<>();

        @Builder.Default
        private Set<String> teamMembershipIds = new HashSet<>();
}
