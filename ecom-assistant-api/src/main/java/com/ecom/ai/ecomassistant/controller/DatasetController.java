package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.db.config.FileStorageProperties;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import com.ecom.ai.ecomassistant.db.model.Document;
import com.ecom.ai.ecomassistant.db.service.DatasetService;
import com.ecom.ai.ecomassistant.db.service.DocumentService;
import com.ecom.ai.ecomassistant.event.file.AiFileUploadEvent;
import com.ecom.ai.ecomassistant.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.model.dto.request.DatasetCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.FileUploadRequest;
import com.ecom.ai.ecomassistant.model.dto.response.DatasetDetailResponse;
import com.ecom.ai.ecomassistant.resource.StorageType;
import com.ecom.ai.ecomassistant.resource.file.FileInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    private final DocumentService documentService;

    private final FileStorageProperties fileStorageProperties;

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/{id}")
    public DatasetDetailResponse getDatasetDetail(@PathVariable String id) {
        Dataset dataset = datasetService
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

        return DatasetDetailResponse.builder()
                .dataset(dataset)
                .documents(documentService.findAllByDatasetId(id))
                .build();
    }

    @PostMapping
    public Dataset createDataset(@RequestBody @Valid DatasetCreateRequest datasetCreateRequest) {

        Dataset dataset = new Dataset();
        //TODO get user from header
        //dataset.setUserId();
        dataset.setName(datasetCreateRequest.getName());
        dataset.setDescription(datasetCreateRequest.getDescription());
        dataset.setPermission(datasetCreateRequest.getPermission());

        return datasetService.createDataset(dataset);
    }

    //@Transactional
    @PostMapping(value = "/{datasetId}/with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @PathVariable String datasetId,
            @Valid @ModelAttribute FileUploadRequest request
    ) throws IOException {

        if (datasetService.findById(datasetId).isEmpty()) {
            return ResponseEntity.badRequest().body("Dataset not found");
        }

        MultipartFile file = request.getFile();
        String fileName = file.getOriginalFilename();

        //TODO get user from header
        String userId = "user-123";
        Path uploadDirPath = Paths.get(fileStorageProperties.getUploadDir(), datasetId, userId);
        Files.createDirectories(uploadDirPath);

        Path destinationFile = uploadDirPath.resolve(fileName);
        file.transferTo(destinationFile);
        String fullPath = destinationFile.toAbsolutePath().toString();

        Document document = Document.builder()
                .datasetId(datasetId)
                .fileName(fileName)
                .fullPath(fullPath)
                .timestamp(Instant.now().toEpochMilli())
                .storageType(StorageType.LOCAL)
                .build();
        documentService.save(document);

        AiFileUploadEvent event = AiFileUploadEvent.builder()
                .datasetId(datasetId)
                .documentId(document.getId())
                .userId(null) //TODO
                .fileInfo(FileInfo.builder()
                        .fileId(document.getId())
                        .fileName(fileName)
                        .fullPath(fullPath)
                        .build()
                )
                .build();

        eventPublisher.publishEvent(event);

        return ResponseEntity.ok("File uploaded successfully: " + fullPath);
    }

    @PatchMapping("/{id}")
    public Dataset updateDataset(
            @PathVariable String id,
            @Valid @RequestBody Dataset updatedDataset) {
        return datasetService.updateDataset(id, updatedDataset);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDataset(@PathVariable String id) {
        boolean deleted = datasetService.deleteDataset(id);

        if (deleted) {
            return ResponseEntity.ok("Delete successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dataset not found");
        }
    }

    @GetMapping
    public List<Dataset> getAllDatasets() {
        return datasetService.findAll();
    }
}
