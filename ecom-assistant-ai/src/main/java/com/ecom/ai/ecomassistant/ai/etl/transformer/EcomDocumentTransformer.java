package com.ecom.ai.ecomassistant.ai.etl.transformer;

import org.springframework.ai.document.Document;

import java.util.List;

public interface EcomDocumentTransformer {

    List<Document> transform(List<Document> documents);
}
