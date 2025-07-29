package com.ecom.ai.ecomassistant.ai.service;

import com.ecom.ai.ecomassistant.db.model.QAPair;
import com.ecom.ai.ecomassistant.db.service.QAVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QAVectorizationService {

    private static final Logger log = LoggerFactory.getLogger(QAVectorizationService.class);
    
    private final VectorStore qaVectorStore; // 專用於 QA 的向量存儲，指向 qa-vector collection
    private final QAVerificationService qaVerificationService;

    public QAVectorizationService(
            @Qualifier("qaVectorStore") VectorStore qaVectorStore,
            QAVerificationService qaVerificationService) {
        this.qaVectorStore = qaVectorStore;
        this.qaVerificationService = qaVerificationService;
    }

    /**
     * 向量化已批准的 QA
     */
    @Async
    public void vectorizeApprovedQAs() {
        List<QAPair> approvedQAs = qaVerificationService.getApprovedNotVectorizedQAs();
        
        if (approvedQAs.isEmpty()) {
            log.info("No approved QAs to vectorize");
            return;
        }

        log.info("Starting vectorization of {} approved QAs", approvedQAs.size());

        for (QAPair qaPair : approvedQAs) {
            try {
                vectorizeQA(qaPair);
            } catch (Exception e) {
                log.error("Failed to vectorize QA: {}", qaPair.getId(), e);
            }
        }

        log.info("Completed vectorization of {} QAs", approvedQAs.size());
    }

    /**
     * 向量化單個 QA
     */
    public void vectorizeQA(QAPair qaPair) {
        try {
            // 建立向量文檔，使用問題和答案的組合作為內容
            String content = formatQAContent(qaPair);
            
            // 建立元資料
            Map<String, Object> metadata = buildQAMetadata(qaPair);
            
            // 建立文檔
            Document document = new Document(qaPair.getId(), content, metadata);
            
            // 向量化並存儲到 qa-vector collection
            qaVectorStore.add(List.of(document));
            
            // 標記為已向量化
            qaVerificationService.markAsVectorized(qaPair.getId(), qaPair.getId());
            
            log.info("Successfully vectorized QA: {}", qaPair.getId());
            
        } catch (Exception e) {
            log.error("Failed to vectorize QA: {}", qaPair.getId(), e);
            throw new RuntimeException("Vectorization failed for QA: " + qaPair.getId(), e);
        }
    }

    /**
     * 格式化 QA 內容用於向量化
     */
    private String formatQAContent(QAPair qaPair) {
        return String.format("問題: %s\n\n答案: %s", 
            qaPair.getQuestion(), 
            qaPair.getAnswer());
    }

    /**
     * 建立 QA 元資料
     */
    private Map<String, Object> buildQAMetadata(QAPair qaPair) {
        Map<String, Object> metadata = new HashMap<>();
        
        // 基本資訊
        metadata.put("id", qaPair.getId());
        metadata.put("datasetId", qaPair.getDatasetId());
        metadata.put("datasetName", qaPair.getDatasetName());
        metadata.put("documentId", qaPair.getDocumentId());
        metadata.put("documentName", qaPair.getDocumentName());
        metadata.put("fileName", qaPair.getFileName());
        metadata.put("questionIndex", qaPair.getQuestionIndex());
        
        // 內容類型標記
        metadata.put("contentType", "qa_pair");
        metadata.put("source", "verified_qa");
        
        // 驗證資訊
        metadata.put("verificationStatus", qaPair.getVerificationStatus().toString());
        metadata.put("verifiedBy", qaPair.getVerifiedBy());
        
        // 標籤資訊
        if (qaPair.getTags() != null && !qaPair.getTags().isEmpty()) {
            metadata.put("tags", qaPair.getTags());
        }
        
        // 時間戳 - 轉換為字串避免 Jackson 序列化問題
        if (qaPair.getCreatedAt() != null) {
            metadata.put("createdAt", qaPair.getCreatedAt().toString());
        }
        if (qaPair.getVerifiedAt() != null) {
            metadata.put("verifiedAt", qaPair.getVerifiedAt().toString());
        }
        
        return metadata;
    }

    /**
     * 移除已向量化的 QA（當原始 QA 被拒絕或刪除時）
     */
    public void removeVectorizedQA(String qaId) {
        try {
            qaVectorStore.delete(List.of(qaId));
            log.info("Removed vectorized QA: {}", qaId);
        } catch (Exception e) {
            log.error("Failed to remove vectorized QA: {}", qaId, e);
        }
    }

    /**
     * 批量向量化指定的 QA 列表
     */
    public void batchVectorizeQAs(List<QAPair> qaPairs) {
        log.info("Starting batch vectorization of {} QAs", qaPairs.size());
        
        for (QAPair qaPair : qaPairs) {
            try {
                vectorizeQA(qaPair);
            } catch (Exception e) {
                log.error("Failed to vectorize QA in batch: {}", qaPair.getId(), e);
            }
        }
        
        log.info("Completed batch vectorization");
    }

    /**
     * 異步向量化指定 QA ID 列表
     */
    @Async
    public void vectorizeApprovedQAsByIds(List<String> qaIds) {
        log.info("Starting vectorization of {} specific QAs", qaIds.size());
        
        List<QAPair> approvedQAs = qaVerificationService.getApprovedNotVectorizedQAs()
                .stream()
                .filter(qa -> qaIds.contains(qa.getId()))
                .toList();
        
        if (approvedQAs.isEmpty()) {
            log.warn("No approved QAs found for the specified IDs: {}", qaIds);
            return;
        }

        for (QAPair qaPair : approvedQAs) {
            try {
                vectorizeQA(qaPair);
            } catch (Exception e) {
                log.error("Failed to vectorize QA: {}", qaPair.getId(), e);
            }
        }

        log.info("Completed vectorization of {} QAs from specified list", approvedQAs.size());
    }
}