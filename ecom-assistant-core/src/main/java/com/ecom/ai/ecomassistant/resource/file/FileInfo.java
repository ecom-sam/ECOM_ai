package com.ecom.ai.ecomassistant.resource.file;

import com.ecom.ai.ecomassistant.resource.StorageType;

public record FileInfo(
        String fileId,
        String fileName,
        String fullPath,
        long timestamp,
        StorageType storageType
) {
}
