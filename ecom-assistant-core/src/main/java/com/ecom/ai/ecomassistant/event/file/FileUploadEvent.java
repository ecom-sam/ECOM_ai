package com.ecom.ai.ecomassistant.event.file;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadEvent extends NewFileEvent {
    protected String userId;

    public FileUploadEvent(String userId) {
        this.userId = userId;
    }

    public FileUploadEvent() {}
}
