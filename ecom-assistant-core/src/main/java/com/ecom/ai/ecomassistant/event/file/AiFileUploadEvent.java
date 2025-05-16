package com.ecom.ai.ecomassistant.event.file;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiFileUploadEvent extends FileUploadEvent implements AiNewFileEvent {
    private String datasetId;
    private String documentId;
}
