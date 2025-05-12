package com.ecom.ai.ecomassistant.ai.etl.reader;

import org.springframework.ai.document.Document;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.util.List;

public interface EcomDocumentReader {

    List<Document> read(Resource resource);

    default List<Document> read(String path) {
        Resource resource = new PathResource(path);
        return read(resource);
    }
}
