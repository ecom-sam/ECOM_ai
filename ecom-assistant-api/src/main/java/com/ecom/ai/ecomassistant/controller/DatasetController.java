package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.core.service.DatasetManager;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import com.ecom.ai.ecomassistant.db.service.DocumentService;
import com.ecom.ai.ecomassistant.model.dto.mapper.DatasetMapper;
import com.ecom.ai.ecomassistant.model.dto.request.DatasetCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.FileUploadRequest;
import com.ecom.ai.ecomassistant.model.dto.response.DatasetDetailResponse;
import com.ecom.ai.ecomassistant.model.dto.response.PageResponse;
import com.ecom.ai.ecomassistant.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/datasets")
public class DatasetController {

    private final DatasetManager datasetManager;
    private final DocumentService documentService;

    @GetMapping("/{id}")
    public DatasetDetailResponse getDatasetDetail(@PathVariable String id, @CurrentUserId String userId) {
        Dataset dataset = datasetManager.getDatasetDetail(id, userId);
        return DatasetDetailResponse.builder()
                .dataset(dataset)
                .documents(documentService.findAllByDatasetId(id))
                .build();
    }

    @PostMapping
    public Dataset createDataset(@RequestBody @Valid DatasetCreateRequest datasetCreateRequest) {
        Dataset dataset = DatasetMapper.INSTANCE.toEntity(datasetCreateRequest);
        return datasetManager.createDataset(dataset);
    }

    @PostMapping(value = "/{datasetId}/with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @CurrentUserId String userId,
            @PathVariable String datasetId,
            @Valid @ModelAttribute FileUploadRequest request
    ) throws IOException {
        String fullPath = datasetManager.uploadFile(userId, datasetId, request.getFile());
        return ResponseEntity.ok("File uploaded successfully: " + fullPath);
    }

    @PatchMapping("/{id}")
    public Dataset updateDataset(
            @PathVariable String id,
            @Valid @RequestBody Dataset updatedDataset) {
        return datasetManager.updateDataset(id, updatedDataset);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDataset(@PathVariable String id) {
        boolean deleted = datasetManager.deleteDataset(id);

        if (deleted) {
            return ResponseEntity.ok("Delete successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dataset not found");
        }
    }

    @GetMapping
    public PageResponse<Dataset> findDatasetsByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdDateTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUserId String userId
    ) {
        Pageable pageable = PageUtil.buildPageable(page, limit, sortBy, sortDir);
        var pageResult = datasetManager.findVisibleDatasets(userId, name, pageable);
        return PageResponse.of(pageResult);
    }
    
    /**
     * 獲取用戶有權限的所有知識庫（供對話頁面使用）
     * 直接複用現有的權限檢查邏輯
     */
    @GetMapping("/for-chat")
    public ResponseEntity<List<DatasetDetailResponse>> getDatasetsForChat(
            @CurrentUserId String userId) {
        
        // 直接使用現有的 DatasetManager 方法
        List<Dataset> datasets = datasetManager.findVisibleDatasetsForChat(userId);
        List<DatasetDetailResponse> response = datasets.stream()
            .map(dataset -> DatasetDetailResponse.builder()
                .dataset(dataset)
                .documents(documentService.findAllByDatasetId(dataset.getId()))
                .build())
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新知識庫的 tags
     */
    @PutMapping("/{id}/tags")
    public ResponseEntity<Dataset> updateDatasetTags(
            @PathVariable String id,
            @RequestBody @Valid UpdateTagsRequest request,
            @CurrentUserId String userId) {
        
        Dataset updatedDataset = datasetManager.updateDatasetTags(id, request.getTags(), userId);
        return ResponseEntity.ok(updatedDataset);
    }
    
    // 內部類別：更新 tags 的請求
    public static class UpdateTagsRequest {
        @jakarta.validation.constraints.Size(max = 3, message = "最多只能設置 3 個 tags")
        private java.util.Set<String> tags = new java.util.HashSet<>();
        
        public java.util.Set<String> getTags() { return tags; }
        public void setTags(java.util.Set<String> tags) { this.tags = tags; }
    }
}
