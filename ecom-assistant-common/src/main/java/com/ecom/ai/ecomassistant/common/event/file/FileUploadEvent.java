package com.ecom.ai.ecomassistant.common.event.file;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class FileUploadEvent extends NewFileEvent {
    protected String userId;
}
