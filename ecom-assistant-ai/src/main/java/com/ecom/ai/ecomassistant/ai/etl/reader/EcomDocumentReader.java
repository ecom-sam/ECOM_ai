package com.ecom.ai.ecomassistant.ai.etl.reader;

import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import org.springframework.ai.document.Document;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.util.List;

public interface EcomDocumentReader {

    List<Document> read(Resource resource);

    default List<Document> read(FileInfo fileInfo) {
        Resource resource = new PathResource(fileInfo.fullPath());
        return read(resource);
    }
}
