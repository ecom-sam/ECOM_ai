package com.ecom.ai.ecomassistant.ai.event.file;

import com.ecom.ai.ecomassistant.common.event.file.FileUploadEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class AiFileUploadEvent extends FileUploadEvent implements AiNewFileEvent {
    private String datasetId;
    private String documentId;
}
