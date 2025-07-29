package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.QAPair;
import com.ecom.ai.ecomassistant.db.repository.QAPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QAVerificationService {

    private final QAPairRepository qaPairRepository;

    /**
     * 獲取待驗證的 QA 列表（分頁）
     */
    public Page<QAPair> getPendingQAs(Pageable pageable) {
        return qaPairRepository.findByVerificationStatus(QAPair.VerificationStatus.PENDING, pageable);
    }

    /**
     * 獲取所有待驗證的 QA 數量
     */
    public Long getPendingQACount() {
        return qaPairRepository.countPendingQAs();
    }

    /**
     * 批准 QA
     */
    public QAPair approveQA(String qaId, String verifierId, String note) {
        Optional<QAPair> qaPairOpt = qaPairRepository.findById(qaId);
        if (qaPairOpt.isEmpty()) {
            throw new RuntimeException("QA not found: " + qaId);
        }

        QAPair qaPair = qaPairOpt.get();
        qaPair.setVerificationStatus(QAPair.VerificationStatus.APPROVED);
        qaPair.setVerifiedBy(verifierId);
        qaPair.setVerifiedAt(LocalDateTime.now());
        qaPair.setVerificationNote(note);
        qaPair.setUpdatedAt(LocalDateTime.now());

        log.info("QA approved: {} by {}", qaId, verifierId);
        return qaPairRepository.save(qaPair);
    }

    /**
     * 拒絕 QA
     */
    public QAPair rejectQA(String qaId, String verifierId, String note) {
        Optional<QAPair> qaPairOpt = qaPairRepository.findById(qaId);
        if (qaPairOpt.isEmpty()) {
            throw new RuntimeException("QA not found: " + qaId);
        }

        QAPair qaPair = qaPairOpt.get();
        qaPair.setVerificationStatus(QAPair.VerificationStatus.REJECTED);
        qaPair.setVerifiedBy(verifierId);
        qaPair.setVerifiedAt(LocalDateTime.now());
        qaPair.setVerificationNote(note);
        qaPair.setUpdatedAt(LocalDateTime.now());

        log.info("QA rejected: {} by {}", qaId, verifierId);
        return qaPairRepository.save(qaPair);
    }

    /**
     * 批量批准 QA 並觸發向量化
     */
    public void batchApproveQAs(List<String> qaIds, String verifierId, String note) {
        // 1. 批准所有 QA
        qaIds.forEach(qaId -> approveQA(qaId, verifierId, note));
        log.info("Batch approved {} QAs by {}", qaIds.size(), verifierId);
        
        // 2. 異步觸發向量化 (需要在調用方注入 QAVectorizationService)
        // 這裡不直接注入 AI 模組的服務，避免循環依賴
        // 向量化將在 Controller 層觸發
    }

    /**
     * 批量拒絕 QA
     */
    public void batchRejectQAs(List<String> qaIds, String verifierId, String note) {
        qaIds.forEach(qaId -> rejectQA(qaId, verifierId, note));
        log.info("Batch rejected {} QAs by {}", qaIds.size(), verifierId);
    }

    /**
     * 獲取已批準但未向量化的 QA
     */
    public List<QAPair> getApprovedNotVectorizedQAs() {
        return qaPairRepository.findApprovedNotVectorized();
    }

    /**
     * 標記 QA 為已向量化
     */
    public void markAsVectorized(String qaId, String vectorId) {
        Optional<QAPair> qaPairOpt = qaPairRepository.findById(qaId);
        if (qaPairOpt.isPresent()) {
            QAPair qaPair = qaPairOpt.get();
            qaPair.setVectorized(true);
            qaPair.setVectorId(vectorId);
            qaPair.setUpdatedAt(LocalDateTime.now());
            qaPairRepository.save(qaPair);
            log.info("QA marked as vectorized: {} -> {}", qaId, vectorId);
        }
    }

    /**
     * 獲取指定資料集的已驗證 QA
     */
    public List<QAPair> getVerifiedQAs(String datasetId, QAPair.VerificationStatus status) {
        return qaPairRepository.findByDatasetIdAndVerificationStatus(datasetId, status);
    }
}