package com.ecom.ai.ecomassistant.db.config;

import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.codec.JsonTranscoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.cache.CouchbaseCacheConfiguration;
import org.springframework.data.couchbase.cache.CouchbaseCacheManager;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

@Configuration
@RequiredArgsConstructor
public class CouchbaseCacheConfig {

    private final CouchbaseCacheProperties properties;

    @Bean
    public CouchbaseCacheManager cacheManager(CouchbaseTemplate template) {

        JacksonJsonSerializer jsonSerializer = JacksonJsonSerializer.create();

        JsonTranscoder jsonTranscoder = JsonTranscoder.create(jsonSerializer);

        CouchbaseCacheManager.CouchbaseCacheManagerBuilder builder = CouchbaseCacheManager
                .builder(template.getCouchbaseClientFactory().withScope(properties.getScopeName()))
                .cacheDefaults(CouchbaseCacheConfiguration
                        .defaultCacheConfig()
                        .disableCachingNullValues()
                        .valueTranscoder(jsonTranscoder)
                        .collection(properties.getCollectionName())
                );

        return builder.build();
    }

}
