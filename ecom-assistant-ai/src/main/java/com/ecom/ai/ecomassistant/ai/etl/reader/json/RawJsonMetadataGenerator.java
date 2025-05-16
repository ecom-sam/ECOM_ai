package com.ecom.ai.ecomassistant.ai.etl.reader.json;

import org.springframework.ai.reader.JsonMetadataGenerator;

import java.util.Map;

public class RawJsonMetadataGenerator implements JsonMetadataGenerator {
    @Override
    public Map<String, Object> generate(Map<String, Object> jsonMap) {
        return Map.of("raw", jsonMap);
    }
}
