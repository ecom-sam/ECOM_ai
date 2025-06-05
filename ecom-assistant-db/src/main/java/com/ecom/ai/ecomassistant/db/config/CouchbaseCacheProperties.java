package com.ecom.ai.ecomassistant.db.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.cache.couchbase")
@Component
@Getter
@Setter
public class CouchbaseCacheProperties {
    private String scopeName = "AI";
    private String collectionName = "cache";
}
