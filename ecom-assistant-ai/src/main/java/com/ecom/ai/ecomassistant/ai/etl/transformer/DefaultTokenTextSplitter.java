package com.ecom.ai.ecomassistant.ai.etl.transformer;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultTokenTextSplitter implements EcomDocumentTransformer {

    @Override
    public List<Document> transform(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, false);
        return splitter.apply(documents);
    }
}
