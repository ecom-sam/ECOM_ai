package com.ecom.ai.ecomassistant.ai.event.file;

public interface AiNewFileEvent {
    void setDatasetId(String id);
    String getDatasetId();

    void setDocumentId(String id);
    String getDocumentId();
}
