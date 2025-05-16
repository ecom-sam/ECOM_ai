package com.ecom.ai.ecomassistant.ai.etl;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FileProcessingRule {
    private String name;
    private String reader;
    private List<String> transformers;
}
