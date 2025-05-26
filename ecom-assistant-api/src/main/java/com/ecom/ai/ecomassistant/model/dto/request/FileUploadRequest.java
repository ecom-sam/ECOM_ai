package com.ecom.ai.ecomassistant.model.dto.request;

import com.ecom.ai.ecomassistant.common.resource.Permission;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class FileUploadRequest {
    @NotNull(message = "File must not be null")
    private MultipartFile file;

    @NotNull(message = "Permission must not be null")
    private Permission permission;
}

