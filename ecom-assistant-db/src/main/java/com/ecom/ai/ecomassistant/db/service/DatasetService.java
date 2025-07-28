package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.repository.DatasetRepository;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DatasetService extends CrudService<Dataset, String, DatasetRepository> {

    protected DatasetService(DatasetRepository repository) {
        super(repository);
    }

    public Page<Dataset> searchAll(String name, Pageable pageable) {
        Pageable finalPageable = pageable;

        // 檢查是否已經有 name 排序，如果沒有才加入
        boolean hasNameSort = pageable.getSort().stream()
                .anyMatch(order -> "name".equals(order.getProperty()));

        if (!hasNameSort) {
            Sort nameSort = Sort.by("name").ascending();
            Sort combinedSort = pageable.getSort().and(nameSort);
            
            // 處理 unpaged 情況
            if (pageable.isUnpaged()) {
                finalPageable = Pageable.unpaged(combinedSort);
            } else {
                finalPageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        combinedSort
                );
            }
        }

        if (StringUtils.isEmpty(name)) {
            return repository.findAll(finalPageable);
        } else {
            return repository.searchByCriteria(name, finalPageable);
        }
    }

    public Page<Dataset> findVisibleDatasets(String name, String userId, Set<String> userTeamIds, Pageable pageable) {
        return repository.findVisibleDatasets(name, userId, userTeamIds, pageable);
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
    
    // 新增 tags 相關方法
    
    /**
     * 按多個 tags 查詢知識庫（供管理員使用）
     */
    public List<Dataset> findByTagsIn(List<String> tags) {
        return repository.findByTagsIn(tags);
    }
    
    /**
     * 獲取所有 tags（供管理員使用）
     */
    public List<String> getAllTags() {
        return repository.findDistinctTags();
    }
    
    /**
     * 批次更新 tags（驗證最多 3 個）
     */
    public void updateTagsForDatasets(List<String> datasetIds, Set<String> newTags) {
        if (newTags != null && newTags.size() > 3) {
            throw new IllegalArgumentException("Dataset 最多只能設置 3 個 tags");
        }
        List<Dataset> datasets = findAllById(datasetIds);
        datasets.forEach(dataset -> dataset.setTags(newTags));
        saveAll(datasets);
    }
}
