package com.ecom.ai.ecomassistant.event.listener;

import com.ecom.ai.ecomassistant.ai.etl.transformer.DefaultDatasetInfoEnricher;
import com.ecom.ai.ecomassistant.ai.service.EtlService;
import com.ecom.ai.ecomassistant.ai.event.file.AiFileUploadEvent;
import com.ecom.ai.ecomassistant.common.context.DatasetContext;
import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import com.ecom.ai.ecomassistant.db.model.QAPair;
import com.ecom.ai.ecomassistant.db.service.DatasetService;
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
    private final DatasetService datasetService;

    @Async
    @SneakyThrows
    @EventListener
    public void onAiFileUploadEvent(AiFileUploadEvent event) {
        FileInfo fileInfo = event.getFileInfo();
        var datasetId = event.getDatasetId();
        var documentId = event.getDocumentId();
        
        log.info("Processing file upload with Q/A generation: {} (dataset: {}, document: {})", 
                fileInfo.fileName(), datasetId, documentId);

        try {
            DatasetContext.setDatasetContextData(datasetId, documentId);
            
            // Get dataset info for Q/A tagging
            String datasetName = "";
            java.util.Set<String> datasetTags = new java.util.HashSet<>();
            try {
                var dataset = datasetService.findById(datasetId);
                if (dataset.isPresent()) {
                    datasetName = dataset.get().getName();
                    datasetTags = dataset.get().getTags() != null ? dataset.get().getTags() : new java.util.HashSet<>();
                }
            } catch (Exception e) {
                log.warn("Could not retrieve dataset info for {}: {}", datasetId, e.getMessage());
                datasetName = "Unknown Dataset";
            }
            
            // Use enhanced processing with Q/A generation
            List<QAPair> qaPairs = etlService.processFileWithQA(fileInfo, datasetId, datasetName, documentId, datasetTags);
            
            log.info("Successfully processed file {} with {} Q/A pairs generated", 
                    fileInfo.fileName(), qaPairs.size());
            
        } catch (Exception e) {
            log.error("Error processing file upload with Q/A generation for {}: {}", 
                    fileInfo.fileName(), e.getMessage(), e);
            
            // Fallback to regular processing if Q/A generation fails
            log.info("Falling back to regular document processing without Q/A generation");
            List<Document> documents = etlService.processFile(fileInfo);
            datasetInfoEnricher.transform(documents);
            etlService.save(documents);
            
        } finally {
            DatasetContext.clear();
        }
    }
}
