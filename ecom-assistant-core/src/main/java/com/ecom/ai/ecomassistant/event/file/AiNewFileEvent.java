package com.ecom.ai.ecomassistant.event.file;

public interface AiNewFileEvent {
    void setDatasetId(String id);
    String getDatasetId();

    void setDocumentId(String id);
    String getDocumentId();
}
