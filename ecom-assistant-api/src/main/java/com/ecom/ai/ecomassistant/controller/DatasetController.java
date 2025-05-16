package com.ecom.ai.ecomassistant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import com.ecom.ai.ecomassistant.db.service.DatasetService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/datasets")

public class DatasetController {
    private final DatasetService datasetService;

    @PostMapping
    public Dataset createDataset(@RequestBody Dataset dataset) {
        if (dataset.getId() == null || dataset.getId().isEmpty()) {
            dataset.setId(UUID.randomUUID().toString());
        }

        return datasetService.createDataset(dataset);
    }

    @PostMapping("/{id}/with-file")
    public ResponseEntity<String> uploadFile(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("permission") String permission) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }

        if (!("public".equals(permission) || "private".equals(permission) || "group".equals(permission))) {
            return ResponseEntity.badRequest().body("Invalid permission value.");
        }

        try {
            String fileName = file.getOriginalFilename();

            String uploadDirPath = "C:/Users/AllenOoi/uploads";
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File filePath = new File(uploadDir, fileName);
            file.transferTo(filePath);

            return ResponseEntity.ok("File uploaded successfully: " + filePath.getAbsolutePath() + datasetService.findById(id));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Dataset> updateDataset(@PathVariable String id, @RequestBody Dataset updatedDataset) {

        Dataset updated = datasetService.updateDataset(id, updatedDataset);
        return ResponseEntity.ok(updated);
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
    public ResponseEntity<List<Dataset>> getAllDatasets() {
        List<Dataset> datasets = datasetService.getAllDatasets();  // Get all datasets via service
        return ResponseEntity.ok(datasets);
    }
}
