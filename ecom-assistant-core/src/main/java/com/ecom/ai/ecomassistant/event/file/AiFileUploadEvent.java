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
        super(userId); // Call the constructor of FileUploadEvent
        this.datasetId = datasetId;
        this.documentId = documentId;
        this.fileInfo = fileInfo; // Initialize the fileInfo from NewFileEvent
    }

    // Constructor for easier instantiation (without fileInfo)
    public AiFileUploadEvent(String userId, String datasetId, String documentId) {
        this(userId, datasetId, documentId, null); // Optional: default fileInfo to null if not provided
    }
}
