package com.ecom.ai.ecomassistant.db.service;


import com.ecom.ai.ecomassistant.db.repository.DatasetRepository;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatasetService extends CrudService<Dataset, String, DatasetRepository> {

    protected DatasetService(DatasetRepository repository) {
        super(repository);
    }

    // Create new Dataset
    public Dataset createDataset(Dataset dataset) {
        return repository.save(dataset);
    }

    // Update existing Dataset
    public Dataset updateDataset(String id, Dataset updatedDataset) {
        // Fetch the existing dataset
        Dataset existingDataset = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset not found with id: " + id));

        // Update the fields that are not null in the incoming request
        if (updatedDataset.getName() != null) {
            existingDataset.setName(updatedDataset.getName());
        }
        if (updatedDataset.getDescription() != null) {
            existingDataset.setDescription(updatedDataset.getDescription());
        }
        if (updatedDataset.getPermission() != null) {
            existingDataset.setPermission(updatedDataset.getPermission());
        }

        // Save the updated dataset
        return repository.save(existingDataset);
    }

    public boolean deleteDataset(String id) {
        // Check if the dataset exists
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;  // Deletion successful
        } else {
            return false;  // Dataset not found
        }
    }

    public List<Dataset> getAllDatasets() {
        return repository.findAll();  // Fetch all datasets from the DB
    }

    public Optional<Dataset> getDatasetById(String id) { return repository.findById(id); }
}
