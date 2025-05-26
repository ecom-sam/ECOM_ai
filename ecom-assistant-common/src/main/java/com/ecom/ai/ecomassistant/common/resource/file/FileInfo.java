package com.ecom.ai.ecomassistant.common.resource.file;

import com.ecom.ai.ecomassistant.common.resource.StorageType;
import lombok.Builder;

@Builder
public record FileInfo(
        String fileId,
        String fileName,
        String fullPath,
        long timestamp,
        StorageType storageType
) {
}
