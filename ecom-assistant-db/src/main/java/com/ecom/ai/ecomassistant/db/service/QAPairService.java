package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.QAPair;
import com.ecom.ai.ecomassistant.db.repository.QAPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class QAPairService extends CrudService<QAPair, String, QAPairRepository> {

    private final QAPairRepository qaPairRepository;

    public QAPairService(QAPairRepository qaPairRepository) {
        super(qaPairRepository);
        this.qaPairRepository = qaPairRepository;
    }

    public List<QAPair> findByDatasetId(String datasetId) {
        return qaPairRepository.findByDatasetIdOrderByQuestionIndex(datasetId);
    }

    public List<QAPair> findByDocumentId(String documentId) {
        return qaPairRepository.findByDocumentIdOrderByQuestionIndex(documentId);
    }

    public List<QAPair> findByDatasetIdAndDocumentName(String datasetId, String documentName) {
        return qaPairRepository.findByDatasetIdAndDocumentNameOrderByQuestionIndex(datasetId, documentName);
    }

    public List<QAPair> findByDatasetName(String datasetName) {
        return qaPairRepository.findByDatasetNameOrderByDocumentNameAndQuestionIndex(datasetName);
    }

    public void deleteByDocumentId(String documentId) {
        try {
            qaPairRepository.deleteByDocumentId(documentId);
            log.info("Deleted Q/A pairs for document: {}", documentId);
        } catch (Exception e) {
            log.error("Error deleting Q/A pairs for document {}: {}", documentId, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteByDatasetId(String datasetId) {
        try {
            qaPairRepository.deleteByDatasetId(datasetId);
            log.info("Deleted Q/A pairs for dataset: {}", datasetId);
        } catch (Exception e) {
            log.error("Error deleting Q/A pairs for dataset {}: {}", datasetId, e.getMessage(), e);
            throw e;
        }
    }

    public List<QAPair> saveQAPairs(List<QAPair> qaPairs) {
        try {
            // Generate IDs for new Q/A pairs
            for (QAPair qaPair : qaPairs) {
                if (qaPair.getId() == null) {
                    qaPair.setId(UUID.randomUUID().toString());
                }
            }
            
            List<QAPair> savedPairs = (List<QAPair>) saveAll(qaPairs);
            log.info("Saved {} Q/A pairs", savedPairs.size());
            return savedPairs;
            
        } catch (Exception e) {
            log.error("Error saving Q/A pairs: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void replaceQAPairsForDocument(String documentId, List<QAPair> newQAPairs) {
        try {
            // For now, just save new Q/A pairs without deleting existing ones
            // TODO: Implement proper deletion after fixing query syntax
            saveQAPairs(newQAPairs);
            
            log.info("Saved Q/A pairs for document: {} (count: {})", documentId, newQAPairs.size());
            
        } catch (Exception e) {
            log.error("Error saving Q/A pairs for document {}: {}", documentId, e.getMessage(), e);
            throw e;
        }
    }
}