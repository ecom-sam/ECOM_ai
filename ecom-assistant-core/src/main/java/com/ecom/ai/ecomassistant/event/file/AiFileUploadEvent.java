package com.ecom.ai.ecomassistant.event.file;

import com.ecom.ai.ecomassistant.resource.file.FileInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiFileUploadEvent extends FileUploadEvent implements AiNewFileEvent {
    private String datasetId;
    private String documentId;

    public AiFileUploadEvent(String userId, String datasetId, String documentId, FileInfo fileInfo) {
        super(userId);
        this.datasetId = datasetId;
        this.documentId = documentId;
        this.fileInfo = fileInfo;
    }

    public AiFileUploadEvent(String userId, String datasetId, String documentId) {
        this(userId, datasetId, documentId, null);
    }
}
