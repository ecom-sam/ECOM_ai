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
import com.ecom.ai.ecomassistant.ai.event.file.AiFileUploadEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ecom.ai.ecomassistant.auth.permission.DatasetPermission.DATASET_VIEW;
import static com.ecom.ai.ecomassistant.auth.permission.SystemPermission.SYSTEM_DATASET_ADMIN;

@Service
@RequiredArgsConstructor
public class DatasetManager {

    private final DatasetService datasetService;
    private final DocumentService documentService;
    private final TeamMembershipService teamMembershipService;
    private final FileStorageProperties fileStorageProperties;
    private final ApplicationEventPublisher eventPublisher;

    public Page<Dataset> findVisibleDatasets(String userId, String name, Pageable pageable) {
        boolean canViewAll = PermissionUtil.hasAnyPermission(Set.of(
                SYSTEM_DATASET_ADMIN.getCode()
        ));

        if (canViewAll) {
            return datasetService.searchAll(name, pageable);
        } else {
            Set<String> userTeamIds = teamMembershipService
                    .findAllByUserId(userId).stream()
                    .map(TeamMembership::getTeamId)
                    .collect(Collectors.toSet());
            return datasetService.findVisibleDatasets(name, userId, userTeamIds, pageable);
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

    public Dataset createDataset(Dataset dataset) {
        return datasetService.createDataset(dataset);
    }

    public Dataset updateDataset(String id, Dataset updatedDataset) {
        return datasetService.updateDataset(id, updatedDataset);
    }

    public boolean deleteDataset(String id) {
        return datasetService.deleteDataset(id);
    }

    public String uploadFile(String userId, String datasetId, MultipartFile file) throws IOException {
        if (datasetService.findById(datasetId).isEmpty()) {
            throw new EntityNotFoundException("Dataset not found");
        }

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
}
