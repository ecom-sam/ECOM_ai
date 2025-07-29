package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.ai.service.QAVectorizationService;
import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.core.service.UserManager;
import com.ecom.ai.ecomassistant.db.model.QAPair;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.QAPairService;
import com.ecom.ai.ecomassistant.db.service.QAVerificationService;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/qa")
public class QAController {

    private final QAPairService qaPairService;
    private final QAVerificationService qaVerificationService;
    private final QAVectorizationService qaVectorizationService;
    private final UserService userService;
    private final UserManager userManager;

    /**
     * 獲取指定文檔的 QA 列表
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<QAPair>> getDocumentQAs(
            @PathVariable String documentId,
            @CurrentUserId String userId) {

        // 檢查權限：只有有QA驗證權限的使用者可以查看
        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            List<QAPair> qaPairs = qaPairService.findByDocumentId(documentId);
            log.info("Retrieved {} QA pairs for document: {}", qaPairs.size(), documentId);
            return ResponseEntity.ok(qaPairs);
        } catch (Exception e) {
            log.error("Failed to retrieve QA pairs for document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 獲取指定文檔的 QA 統計資訊
     */
    @GetMapping("/document/{documentId}/stats")
    public ResponseEntity<Map<String, Object>> getDocumentQAStats(
            @PathVariable String documentId,
            @CurrentUserId String userId) {

        // 檢查權限
        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            List<QAPair> qaPairs = qaPairService.findByDocumentId(documentId);
            
            Map<String, Object> stats = new HashMap<>();
            long total = qaPairs.size();
            long pending = qaPairs.stream().mapToLong(qa -> 
                QAPair.VerificationStatus.PENDING.equals(qa.getVerificationStatus()) ? 1 : 0).sum();
            long approved = qaPairs.stream().mapToLong(qa -> 
                QAPair.VerificationStatus.APPROVED.equals(qa.getVerificationStatus()) ? 1 : 0).sum();
            long rejected = qaPairs.stream().mapToLong(qa -> 
                QAPair.VerificationStatus.REJECTED.equals(qa.getVerificationStatus()) ? 1 : 0).sum();

            stats.put("total", total);
            stats.put("pending", pending);
            stats.put("approved", approved);
            stats.put("rejected", rejected);

            log.info("QA stats for document {}: total={}, pending={}, approved={}, rejected={}",
                    documentId, total, pending, approved, rejected);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get QA stats for document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 批量更新 QA 狀態
     */
    @PostMapping("/batch/update-status")
    public ResponseEntity<String> batchUpdateQAStatus(
            @RequestBody @Valid BatchUpdateRequest request,
            @CurrentUserId String userId) {

        // 檢查權限
        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            for (QAStatusUpdate update : request.getUpdates()) {
                if ("APPROVED".equals(update.getStatus())) {
                    qaVerificationService.approveQA(update.getQaId(), userId, update.getNote());
                } else if ("REJECTED".equals(update.getStatus())) {
                    qaVerificationService.rejectQA(update.getQaId(), userId, update.getNote());
                }
            }

            log.info("Successfully updated {} QA statuses by user: {}", request.getUpdates().size(), userId);
            return ResponseEntity.ok("Successfully updated QA statuses");
        } catch (Exception e) {
            log.error("Failed to batch update QA status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to update QA statuses");
        }
    }

    /**
     * 對已通過的 QA 進行向量化
     */
    @PostMapping("/document/{documentId}/vectorize")
    public ResponseEntity<String> vectorizeApprovedQAs(
            @PathVariable String documentId,
            @CurrentUserId String userId) {

        // 檢查權限
        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            // 獲取已批准但未向量化的 QA
            List<QAPair> approvedQAs = qaVerificationService.getApprovedNotVectorizedQAs()
                    .stream()
                    .filter(qa -> documentId.equals(qa.getDocumentId()))
                    .toList();

            if (approvedQAs.isEmpty()) {
                return ResponseEntity.ok("No approved QAs found for vectorization");
            }

            // 觸發向量化 - 逐一向量化
            for (QAPair qaPair : approvedQAs) {
                qaVectorizationService.vectorizeQA(qaPair);
            }

            log.info("Successfully triggered vectorization for {} QAs in document: {}", 
                    approvedQAs.size(), documentId);
            return ResponseEntity.ok("Successfully started vectorization for " + approvedQAs.size() + " QAs");
        } catch (Exception e) {
            log.error("Failed to vectorize QAs for document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to vectorize QAs");
        }
    }

    /**
     * 刪除 QA 向量（當 QA 被拒絕時）
     */
    @DeleteMapping("/{qaId}/vector")
    public ResponseEntity<String> deleteQAVector(
            @PathVariable String qaId,
            @CurrentUserId String userId) {

        // 檢查權限
        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            qaVectorizationService.removeVectorizedQA(qaId);
            
            log.info("Successfully deleted vector for QA: {} by user: {}", qaId, userId);
            return ResponseEntity.ok("Successfully deleted QA vector");
        } catch (Exception e) {
            log.error("Failed to delete vector for QA {}: {}", qaId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete QA vector");
        }
    }

    /**
     * 檢查用戶是否有 QA 驗證權限
     * 只有 SUPER_ADMIN、TEAM_ADMIN 或有 dataset:qa:verification 權限的人可以進行 QA 操作
     */
    private boolean hasQAVerificationPermission(String userId) {
        try {
            log.info("Checking QA verification permission for user: {}", userId);
            
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("User not found: {}", userId);
                return false;
            }
            
            User user = userOpt.get();
            UserManager.UserRoleContext userRoleContext = userManager.getUserRoleContext(user);
            
            log.info("User {} has roles: {} and permissions: {}", userId, userRoleContext.roles(), userRoleContext.permissions());
            
            // 檢查系統角色
            Set<String> allowedRoles = Set.of("system:SUPER_ADMIN", "system:TEAM_ADMIN");
            boolean hasAdminRole = userRoleContext.roles().stream()
                    .anyMatch(allowedRoles::contains);
            
            if (hasAdminRole) {
                log.info("User {} has admin role access", userId);
                return true;
            }
            
            // 檢查是否有特定的 QA 驗證權限
            boolean hasQAPermission = userRoleContext.permissions().contains("dataset:qa:verification");
            log.info("User {} has QA verification permission: {}", userId, hasQAPermission);
            
            return hasQAPermission;
        } catch (Exception e) {
            log.error("Failed to check QA verification permission for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    // DTO 類別
    public static class BatchUpdateRequest {
        private List<QAStatusUpdate> updates;
        
        public List<QAStatusUpdate> getUpdates() { return updates; }
        public void setUpdates(List<QAStatusUpdate> updates) { this.updates = updates; }
    }

    public static class QAStatusUpdate {
        private String qaId;
        private String status;
        private String note;
        
        public String getQaId() { return qaId; }
        public void setQaId(String qaId) { this.qaId = qaId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}