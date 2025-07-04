package com.ecom.ai.ecomassistant.ai.etl.transformer;

import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import org.springframework.ai.document.Document;

import java.util.List;

public interface EcomDocumentTransformer {

    String METADATA_FILE_FULL_PATH = "file_full_path";

    String METADATA_IMAGE_REFERENCE = "image_reference";

    record Base64Result(
            String name,
            long size,
            String base64
    ) {
    }

    List<Document> transform(List<Document> documents);

    static void transform(List<Document> documents, FileInfo fileInfo) {
        for (Document document : documents) {
            document.getMetadata().put(METADATA_FILE_FULL_PATH, fileInfo.fullPath());
        }
    }
}
