package com.ecom.ai.ecomassistant.ai.etl.transformer;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultTableOfContentDetector implements EcomDocumentTransformer {

    @Override
    public List<Document> transform(List<Document> documents) {
//        int maxIndex = Math.min(5, documents.size() -1);
//
//        for (int i = 0; i < 5; i++) {
//            Document document = documents.get(i);
///        }
//
        return List.of();
    }
}
