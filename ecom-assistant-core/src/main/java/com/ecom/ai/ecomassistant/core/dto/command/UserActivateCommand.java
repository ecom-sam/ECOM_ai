package com.ecom.ai.ecomassistant.core.dto.command;



public record UserActivateCommand(
        String id,
        String name,
        String password,
        String passwordConfirm,
        String token
) {
}
