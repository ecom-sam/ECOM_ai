package com.ecom.ai.ecomassistant.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActivateRequest {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordConfirm;

    @NotBlank
    @Schema(example = "ka514c0farp9kqqc")
    private String token;
}
