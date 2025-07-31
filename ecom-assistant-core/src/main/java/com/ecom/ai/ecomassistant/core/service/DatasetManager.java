package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.auth.util.PermissionUtil;
import com.ecom.ai.ecomassistant.common.resource.StorageType;
import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import com.ecom.ai.ecomassistant.core.config.FileStorageProperties;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import com.ecom.ai.ecomassistant.db.model.Document;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.service.DatasetService;
import com.ecom.ai.ecomassistant.db.service.DocumentService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamService;
import com.ecom.ai.ecomassistant.ai.event.file.AiFileUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ecom.ai.ecomassistant.auth.permission.DatasetPermission;
import static com.ecom.ai.ecomassistant.auth.permission.DatasetPermission.*;
import static com.ecom.ai.ecomassistant.auth.permission.SystemPermission.SYSTEM_DATASET_ADMIN;
import static com.ecom.ai.ecomassistant.auth.permission.TeamPermission.TEAM_EDIT;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetManager {

    private final DatasetService datasetService;
    private final DocumentService documentService;
    private final TeamMembershipService teamMembershipService;
    private final TeamService teamService;
    private final FileStorageProperties fileStorageProperties;
    private final ApplicationEventPublisher eventPublisher;

    public Page<Dataset> findVisibleDatasets(String userId, String name, Pageable pageable) {
        log.debug("findVisibleDatasets called for user: {}, name: {}", userId, name);
        
        boolean canViewAll = PermissionUtil.hasAnyPermission(Set.of(
                SYSTEM_DATASET_ADMIN.getCode()
        ));
        
        log.debug("User {} canViewAll: {}", userId, canViewAll);

        if (canViewAll) {
            Page<Dataset> result = datasetService.searchAll(name, pageable);
            log.debug("User {} (admin) found {} datasets", userId, result.getTotalElements());
            return result;
        } else {
            Set<String> userTeamIds = teamMembershipService
                    .findAllByUserId(userId).stream()
                    .map(TeamMembership::getTeamId)
                    .collect(Collectors.toSet());
            log.debug("User {} teamIds: {}", userId, userTeamIds);
            
            try {
                Page<Dataset> result = datasetService.findVisibleDatasets(name, userId, userTeamIds, pageable);
                log.debug("User {} found {} visible datasets", userId, result.getTotalElements());
                return result;
            } catch (Exception e) {
                log.error("Error in findVisibleDatasets for user {}: {}", userId, e.getMessage(), e);
                throw e;
            }
        }
    }

    public Dataset getDatasetDetail(String id, String userId) {
        Dataset dataset = datasetService
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

        // check permission
        Dataset.AccessType accessType = Optional.ofNullable(dataset.getAccessType()).orElse(Dataset.AccessType.PRIVATE);
        switch (accessType) {
            case PRIVATE -> {
                boolean isOwner = Objects.equals(dataset.getCreatedBy(), userId);
                if (!isOwner) {
                    PermissionUtil.forbidden();
                }
            }
            case GROUP -> PermissionUtil.checkAnyPermission(Set.of(
                    SYSTEM_DATASET_ADMIN.getCode(),
                    DATASET_VIEW.getCodeWithTeamId(dataset.getTeamId())
            ));
            case PUBLIC -> {
                // do nothing, public access
            }
        }

        return dataset;
    }

    public Dataset createDataset(Dataset dataset, String userId) {
        // Check permissions based on dataset access type
        Dataset.AccessType accessType = Optional.ofNullable(dataset.getAccessType()).orElse(Dataset.AccessType.PRIVATE);
        
        if (accessType == Dataset.AccessType.GROUP) {
            // For GROUP datasets, user must have team edit permission or be system admin
            String teamId = dataset.getTeamId();
            log.debug("Creating GROUP dataset with teamId: {}", teamId);
            
            if (teamId == null || teamId.isEmpty()) {
                throw new IllegalArgumentException("Team ID is required for GROUP access type datasets");
            }
            
            // Verify user belongs to the team (either as member or owner)
            log.debug("Current user ID: {}", userId);
            
            // Check if user has team membership record
            boolean isMember = teamMembershipService.findAllByUserId(userId)
                    .stream()
                    .anyMatch(membership -> Objects.equals(membership.getTeamId(), teamId));
            
            // Check if user is team owner
            boolean isOwner = teamService.findById(teamId)
                    .map(team -> Objects.equals(team.getOwnerId(), userId))
                    .orElse(false);
            
            log.debug("User {} - isMember: {}, isOwner: {}", userId, isMember, isOwner);
            
            if (!isMember && !isOwner) {
                throw new IllegalArgumentException("User does not belong to the specified team");
            }
            
            String requiredPermission = TEAM_EDIT.getCodeWithTeamId(teamId);
            log.debug("Checking permissions: {} or {}", SYSTEM_DATASET_ADMIN.getCode(), requiredPermission);
            
            PermissionUtil.checkAnyPermission(Set.of(
                    SYSTEM_DATASET_ADMIN.getCode(),
                    requiredPermission
            ));
            
            log.debug("Permission check passed for GROUP dataset creation");
        }
        // For PRIVATE datasets, any authenticated user can create (default behavior)
        // For PUBLIC datasets, any authenticated user can create (default behavior)
        
        return datasetService.createDataset(dataset);
    }

    // Keep the old method for backward compatibility
    public Dataset createDataset(Dataset dataset) {
        String currentUserId = (String) SecurityUtils.getSubject().getPrincipal();
        return createDataset(dataset, currentUserId);
    }

    public Dataset updateDataset(String id, Dataset updatedDataset) {
        // Get existing dataset and check permissions
        Dataset existingDataset = datasetService
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

        checkDatasetPermission(existingDataset, DATASET_MANAGE);
        return datasetService.updateDataset(id, updatedDataset);
    }

    public boolean deleteDataset(String id) {
        // Get existing dataset and check permissions
        Dataset dataset = datasetService
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

        checkDatasetPermission(dataset, DATASET_DELETE);
        return datasetService.deleteDataset(id);
    }

    public String uploadFile(String userId, String datasetId, MultipartFile file) throws IOException {
        Dataset dataset = datasetService.findById(datasetId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

        // Check file upload permission
        checkDatasetPermission(dataset, DATASET_FILE_UPLOAD);

        String fileName = file.getOriginalFilename();

        Path uploadDirPath = Paths.get(fileStorageProperties.getUploadDir(), datasetId, userId);
        Files.createDirectories(uploadDirPath);

        Path destinationFile = uploadDirPath.resolve(fileName);
        file.transferTo(destinationFile);
        String fullPath = destinationFile.toAbsolutePath().toString();

        Document document = Document.builder()
                .datasetId(datasetId)
                .fileName(fileName)
                .fullPath(fullPath)
                .storageType(StorageType.LOCAL)
                .build();
        documentService.save(document);

        AiFileUploadEvent event = AiFileUploadEvent.builder()
                .datasetId(datasetId)
                .documentId(document.getId())
                .userId(userId)
                .fileInfo(FileInfo.builder()
                        .fileId(document.getId())
                        .fileName(fileName)
                        .fullPath(fullPath)
                        .build()
                )
                .build();

        eventPublisher.publishEvent(event);

        return fullPath;
    }
    
    /**
     * 獲取用戶有權限的知識庫（供對話頁面使用）
     * 直接複用現有的 findVisibleDatasets 邏輯，不做任何改動
     */
    public List<Dataset> findVisibleDatasetsForChat(String userId) {
        log.info("findVisibleDatasetsForChat called for user: {}", userId);
        try {
            // 直接使用現有方法，只是回傳 List 而不是 Page
            Page<Dataset> page = findVisibleDatasets(userId, "", Pageable.unpaged());
            List<Dataset> result = page.getContent();
            log.info("findVisibleDatasetsForChat for user {} returned {} datasets", userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("Error in findVisibleDatasetsForChat for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新知識庫的 tags
     */
    public Dataset updateDatasetTags(String datasetId, Set<String> tags, String userId) {
        Dataset dataset = datasetService.findById(datasetId)
            .orElseThrow(() -> new RuntimeException("知識庫不存在"));
        
        // Check dataset management permission
        checkDatasetPermission(dataset, DATASET_MANAGE);
        
        // 設置 tags（會自動驗證最多 3 個）
        dataset.setTags(tags);
        
        return datasetService.save(dataset);
    }

    /**
     * Check dataset permission based on access type and user's team membership
     */
    private void checkDatasetPermission(Dataset dataset, DatasetPermission requiredPermission) {
        Dataset.AccessType accessType = Optional.ofNullable(dataset.getAccessType()).orElse(Dataset.AccessType.PRIVATE);
        
        switch (accessType) {
            case PRIVATE -> {
                // For private datasets, only owner or system admin can access
                String currentUserId = (String) SecurityUtils.getSubject().getPrincipal();
                boolean isOwner = Objects.equals(dataset.getCreatedBy(), currentUserId);
                boolean isSystemAdmin = PermissionUtil.hasAnyPermission(Set.of(SYSTEM_DATASET_ADMIN.getCode()));
                
                if (!isOwner && !isSystemAdmin) {
                    PermissionUtil.forbidden();
                }
            }
            case GROUP -> {
                // For group datasets, check team-scoped permissions
                PermissionUtil.checkAnyPermission(Set.of(
                        SYSTEM_DATASET_ADMIN.getCode(),
                        requiredPermission.getCodeWithTeamId(dataset.getTeamId())
                ));
            }
            case PUBLIC -> {
                // For public datasets with write operations, still need team permissions
                if (requiredPermission != DATASET_VIEW) {
                    PermissionUtil.checkAnyPermission(Set.of(
                            SYSTEM_DATASET_ADMIN.getCode(),
                            requiredPermission.getCodeWithTeamId(dataset.getTeamId())
                    ));
                }
                // Read operations on public datasets don't need permission checks
            }
        }
    }
}
