package com.ecom.ai.ecomassistant.event.file;

import com.ecom.ai.ecomassistant.resource.file.FileInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class NewFileEvent {
    protected FileInfo fileInfo;
}
