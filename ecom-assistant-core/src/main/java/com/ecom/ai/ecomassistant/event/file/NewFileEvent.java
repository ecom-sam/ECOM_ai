package com.ecom.ai.ecomassistant.event.file;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class NewFileEvent {
    protected String fileId;
    protected String fileName;
    protected String fullPath;
    protected long timestamp;
}
