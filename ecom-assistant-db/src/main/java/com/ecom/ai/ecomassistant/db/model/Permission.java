package com.ecom.ai.ecomassistant.db.model;

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonCreator;
import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonValue;

public enum Permission {
    PUBLIC,
    PRIVATE,
    GROUP;

    @JsonCreator
    public static Permission fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        try {
            return Permission.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid permission value: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
