package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.ai.service.QAVectorizationService;
import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.core.service.UserManager;
import com.ecom.ai.ecomassistant.db.model.QAPair;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.QAVerificationService;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import com.ecom.ai.ecomassistant.model.dto.response.PageResponse;
import com.ecom.ai.ecomassistant.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/qa-verification")
public class QAVerificationController {

    private final QAVerificationService qaVerificationService;
    private final QAVectorizationService qaVectorizationService;
    private final UserService userService;
    private final UserManager userManager;

    /**
     * 獲取待驗證的 QA 列表（只有團隊管理員可見）
     */
    @GetMapping("/pending")
    public ResponseEntity<PageResponse<QAPair>> getPendingQAs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUserId String userId) {

        // 檢查權限：只有團隊管理員可以查看待驗證的 QA
        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        Pageable pageable = PageUtil.buildPageable(page, limit, sortBy, sortDir);
        Page<QAPair> qaPairs = qaVerificationService.getPendingQAs(pageable);
        
        return ResponseEntity.ok(PageResponse.of(qaPairs));
    }

    /**
     * 批准 QA
     */
    @PostMapping("/{qaId}/approve")
    public ResponseEntity<QAPair> approveQA(
            @PathVariable String qaId,
            @RequestBody(required = false) VerificationRequest request,
            @CurrentUserId String userId) {

        log.info("QA approval request - QA ID: {}, User ID: {}", qaId, userId);
        
        if (!hasQAVerificationPermission(userId)) {
            log.warn("QA verification permission denied for user: {}", userId);
            return ResponseEntity.status(403).build();
        }

        try {
            String note = request != null ? request.getNote() : "";
            log.info("Approving QA {} with note: {}", qaId, note);
            QAPair approvedQA = qaVerificationService.approveQA(qaId, userId, note);
            log.info("QA {} approved successfully", qaId);
            return ResponseEntity.ok(approvedQA);
        } catch (Exception e) {
            log.error("Error approving QA {}: {}", qaId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 拒絕 QA
     */
    @PostMapping("/{qaId}/reject")
    public ResponseEntity<QAPair> rejectQA(
            @PathVariable String qaId,
            @RequestBody(required = false) VerificationRequest request,
            @CurrentUserId String userId) {

        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        String note = request != null ? request.getNote() : "";
        QAPair rejectedQA = qaVerificationService.rejectQA(qaId, userId, note);
        
        return ResponseEntity.ok(rejectedQA);
    }

    /**
     * 批量批准 QA 並自動觸發向量化
     */
    @PostMapping("/batch/approve")
    public ResponseEntity<String> batchApproveQAs(
            @RequestBody @Valid BatchVerificationRequest request,
            @CurrentUserId String userId) {

        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            // 1. 批量批准 QA
            qaVerificationService.batchApproveQAs(request.getQaIds(), userId, request.getNote());
            
            // 2. 異步觸發向量化
            qaVectorizationService.vectorizeApprovedQAsByIds(request.getQaIds());
            
            log.info("Batch approved {} QAs and triggered vectorization for user: {}", 
                    request.getQaIds().size(), userId);
            
            return ResponseEntity.ok("批量批准成功，正在進行向量化處理");
        } catch (Exception e) {
            log.error("Error in batch approve QAs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("批准過程中發生錯誤");
        }
    }

    /**
     * 批量拒絕 QA
     */
    @PostMapping("/batch/reject")
    public ResponseEntity<String> batchRejectQAs(
            @RequestBody @Valid BatchVerificationRequest request,
            @CurrentUserId String userId) {

        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        qaVerificationService.batchRejectQAs(request.getQaIds(), userId, request.getNote());
        
        return ResponseEntity.ok("批量拒絕成功");
    }

    /**
     * 獲取待驗證 QA 數量
     */
    @GetMapping("/pending/count")
    public ResponseEntity<Long> getPendingQACount(@CurrentUserId String userId) {
        
        if (!hasQAVerificationPermission(userId)) {
            return ResponseEntity.status(403).build();
        }

        Long count = qaVerificationService.getPendingQACount();
        return ResponseEntity.ok(count);
    }

    /**
     * 檢查用戶是否有 QA 驗證權限
     * 只有 SUPER_ADMIN、TEAM_ADMIN 或有 qa.verification 權限的人可以驗證 QA
     */
    private boolean hasQAVerificationPermission(String userId) {
        try {
            log.info("Checking QA verification permission for user: {}", userId);
            
            // 使用 UserManager 直接檢查用戶角色和權限，避免 Shiro 授權問題
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("User not found: {}", userId);
                return false;
            }
            
            User user = userOpt.get();
            UserManager.UserRoleContext userRoleContext = userManager.getUserRoleContext(user);
            
            log.info("User {} has roles: {} and permissions: {}", userId, userRoleContext.roles(), userRoleContext.permissions());
            
            // 檢查系統角色 (只有 SUPER_ADMIN 和 TEAM_ADMIN)
            Set<String> allowedRoles = Set.of("system:SUPER_ADMIN", "system:TEAM_ADMIN");
            boolean hasAdminRole = userRoleContext.roles().stream()
                    .anyMatch(allowedRoles::contains);
            
            if (hasAdminRole) {
                log.info("User {} has admin role access", userId);
                return true;
            }
            
            // 檢查是否有特定的 QA 驗證權限
            boolean hasQAPermission = userRoleContext.permissions().contains("qa.verification");
            log.info("User {} has QA verification permission: {}", userId, hasQAPermission);
            
            return hasQAPermission;
        } catch (Exception e) {
            log.error("Failed to check QA verification permission for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    // DTO 類別
    public static class VerificationRequest {
        private String note;
        
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class BatchVerificationRequest {
        private List<String> qaIds;
        private String note;
        
        public List<String> getQaIds() { return qaIds; }
        public void setQaIds(List<String> qaIds) { this.qaIds = qaIds; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}