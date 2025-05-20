package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.Document;
import com.ecom.ai.ecomassistant.db.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService extends CrudService<Document, String, DocumentRepository> {

    protected DocumentService(DocumentRepository repository) {
        super(repository);
    }

    public List<Document> findAllByDatasetId(String datasetId) {
        return repository.findAllByDatasetId(datasetId);
    }
}
