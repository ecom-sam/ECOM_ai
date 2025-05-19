package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.Document;
import com.ecom.ai.ecomassistant.db.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class DocumentService extends CrudService<Document, String, DocumentRepository> {

    protected DocumentService(DocumentRepository repository) {
        super(repository);
    }
}
