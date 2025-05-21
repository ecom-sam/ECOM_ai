package com.ecom.ai.ecomassistant.ai.etl;

import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProcessingRuleResolver {

    private final Map<String, FileProcessingRule> rules;
    private final FileProcessingRule defaultFileProcessingRule;

    public FileProcessingRule resolve(FileInfo fileInfo) {
        return rules.getOrDefault("ruleName", defaultFileProcessingRule);
    }
}
