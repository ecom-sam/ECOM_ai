package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.db.config.FileStorageProperties;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import com.ecom.ai.ecomassistant.db.service.DatasetService;
import com.ecom.ai.ecomassistant.event.file.AiFileUploadEvent;
import com.ecom.ai.ecomassistant.model.dto.request.DatasetCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.FileUploadRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
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
import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/datasets")

public class DatasetController {

    private final DatasetService datasetService;

    private final FileStorageProperties fileStorageProperties;

    private final ApplicationEventPublisher eventPublisher;

    @PostMapping
    public Dataset createDataset(@RequestBody DatasetCreateRequest datasetCreateRequest) {

        Dataset dataset = new Dataset();
        //TODO get user from header
        //dataset.setUserId();
        dataset.setName(datasetCreateRequest.getName());
        dataset.setDescription(datasetCreateRequest.getDescription());
        dataset.setPermission(datasetCreateRequest.getPermission());

        return datasetService.createDataset(dataset);
    }

    @PostMapping("/{id}/with-file")
    public ResponseEntity<String> uploadFile(
            @PathVariable String id,
            @Valid @ModelAttribute FileUploadRequest request
    ) {
        try {
            MultipartFile file = request.getFile();
            String fileName = file.getOriginalFilename();


            //TODO get user from header
            String userId = "user-123";
            Path uploadDirPath = Paths.get(fileStorageProperties.getUploadDir(), id, userId);
            Files.createDirectories(uploadDirPath);

            Path destinationFile = uploadDirPath.resolve(fileName);
            file.transferTo(destinationFile);

            AiFileUploadEvent event = new AiFileUploadEvent(
                    userId,
                    id,
                    UUID.randomUUID().toString()
            );

            eventPublisher.publishEvent(event);

            return ResponseEntity.ok("File uploaded successfully: " +
                    destinationFile.toAbsolutePath() + datasetService.findById(id));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
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
