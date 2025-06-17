package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.auth.Permission;
import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.core.service.UserManager;
import com.ecom.ai.ecomassistant.model.dto.mapper.UserRequestMapper;
import com.ecom.ai.ecomassistant.model.dto.request.LoginRequest;
import com.ecom.ai.ecomassistant.model.dto.request.UserActivateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.UserInviteRequest;
import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManager userManager;

    @GetMapping("/me")
    public UserDto me(@CurrentUserId String userId) {
        return userManager.getUserDetail(userId);
    }

    @PostMapping("/invite")
    @RequiresPermissions({"system:user:invite"})
    public UserDto inviteUser(@RequestBody @Valid UserInviteRequest userInviteRequest) {
        return userManager.inviteUser(userInviteRequest.email());
    }

    @PostMapping("/activate")
    public UserDto activateUser(@RequestBody @Valid UserActivateRequest userActivateRequest) {
        var command = UserRequestMapper.INSTANCE.toUserActivateCommand(userActivateRequest);
        return userManager.activateUser(command);
    }

    @PostMapping("/login")
    public String login(@RequestBody @Valid LoginRequest loginRequest) {
        return userManager.login(loginRequest.getEmail(), loginRequest.getPassword());
    }
}
