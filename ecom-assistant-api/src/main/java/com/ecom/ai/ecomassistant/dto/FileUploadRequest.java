package com.ecom.ai.ecomassistant.dto;

import com.ecom.ai.ecomassistant.db.model.Permission;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public class FileUploadRequest {
    @NotNull(message = "File must not be null")
    private MultipartFile file;

    @NotNull(message = "Permission must not be null")
    private Permission permission;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}

