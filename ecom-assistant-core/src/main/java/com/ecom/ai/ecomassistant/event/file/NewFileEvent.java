package com.ecom.ai.ecomassistant.event.file;

import com.ecom.ai.ecomassistant.resource.file.FileInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class NewFileEvent {
    protected FileInfo fileInfo;
}
