package com.ecom.ai.ecomassistant.ai.event.listener;

import com.ecom.ai.ecomassistant.ai.etl.transformer.DefaultDatasetInfoEnricher;
import com.ecom.ai.ecomassistant.ai.service.ETLService;
import com.ecom.ai.ecomassistant.event.file.AiFileUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFileEventListener {

    private final ETLService etlService;
    private final DefaultDatasetInfoEnricher datasetInfoEnricher;

    @Async
    @EventListener
    public void onAiFileUploadEvent(AiFileUploadEvent event) {
        String fullPath = event.getFullPath();
        List<Document> documents = etlService.processFile(fullPath);
        datasetInfoEnricher.transform(documents);
        etlService.save(documents);
    }
}
