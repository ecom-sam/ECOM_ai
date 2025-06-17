package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.repository.DatasetRepository;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DatasetService extends CrudService<Dataset, String, DatasetRepository> {

    protected DatasetService(DatasetRepository repository) {
        super(repository);
    }

    public Page<Dataset> search(String name, Pageable pageable) {
        if (StringUtils.isEmpty(name)) {
            return repository.findAll(pageable);
        } else {
            return repository.searchByCriteria(name, pageable);
        }
    }

    public Dataset createDataset(Dataset dataset) {
        return repository.save(dataset);
    }

    public Dataset updateDataset(String id, Dataset updatedDataset) {

        Dataset existingDataset = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset not found with id: " + id));

        existingDataset.setName(updatedDataset.getName());
        existingDataset.setDescription(updatedDataset.getDescription());

        return repository.save(existingDataset);
    }

    public boolean deleteDataset(String id) {

        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
