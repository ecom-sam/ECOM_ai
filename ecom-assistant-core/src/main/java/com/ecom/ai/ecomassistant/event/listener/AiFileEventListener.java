package com.ecom.ai.ecomassistant.event.listener;

import com.ecom.ai.ecomassistant.ai.etl.transformer.DefaultDatasetInfoEnricher;
import com.ecom.ai.ecomassistant.ai.service.EtlService;
import com.ecom.ai.ecomassistant.ai.event.file.AiFileUploadEvent;
import com.ecom.ai.ecomassistant.common.context.DatasetContext;
import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    private final EtlService etlService;
    private final DefaultDatasetInfoEnricher datasetInfoEnricher;

    @Async
    @SneakyThrows
    @EventListener
    public void onAiFileUploadEvent(AiFileUploadEvent event) {
        FileInfo fileInfo = event.getFileInfo();
        List<Document> documents = etlService.processFile(fileInfo);

        try {
            var datasetId = event.getDatasetId();
            var documentId = event.getDocumentId();
            DatasetContext.setDatasetContextData(datasetId, documentId);
            datasetInfoEnricher.transform(documents);
        } finally {
            DatasetContext.clear();
        }

        etlService.save(documents);
    }
}
