package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.db.config.FileStorageProperties;
import com.ecom.ai.ecomassistant.db.model.DatasetCreateRequest;
import com.ecom.ai.ecomassistant.dto.FileUploadRequest;
import com.ecom.ai.ecomassistant.event.file.AiFileUploadEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import com.ecom.ai.ecomassistant.db.service.DatasetService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/datasets")

public class DatasetController {
    private final DatasetService datasetService;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping
    public Dataset createDataset(@RequestBody DatasetCreateRequest datasetCreateRequest) {

        Dataset dataset = new Dataset();
        dataset.setUserId(datasetCreateRequest.getUserId());
        dataset.setName(datasetCreateRequest.getName());
        dataset.setDescription(datasetCreateRequest.getDescription());
        dataset.setPermission(datasetCreateRequest.getPermission());

        return datasetService.createDataset(dataset);
    }

    @PostMapping("/{id}/with-file")
    public ResponseEntity<String> uploadFile(
            @PathVariable String id,
            @Valid @ModelAttribute FileUploadRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
        }

        try
        {
            MultipartFile file = request.getFile();
            String fileName = file.getOriginalFilename();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            String uploadDirPath = Paths.get(fileStorageProperties.getUploadDir(), id, userId).toString();
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File destinationFile = new File(uploadDir, fileName);
            file.transferTo(destinationFile);

            AiFileUploadEvent event = new AiFileUploadEvent(
                    userId,
                    id,
                    UUID.randomUUID().toString()
            );

            eventPublisher.publishEvent(event);

            return ResponseEntity.ok("File uploaded successfully: " +
                    destinationFile.getAbsolutePath() + datasetService.findById(id));
        }
        catch (IOException e)
        {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public Dataset updateDataset(
            @PathVariable String id,
            @Valid @RequestBody Dataset updatedDataset)
    {
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
