package com.ecom.ai.ecomassistant.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @Email
    @NotBlank
    @Schema(example = "super_admin@example.com")
    private String email;

    @NotBlank
    private String password;
}
