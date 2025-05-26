package com.ecom.ai.ecomassistant.common.event.file;

import com.ecom.ai.ecomassistant.common.resource.file.FileInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class NewFileEvent {
    protected FileInfo fileInfo;
}
