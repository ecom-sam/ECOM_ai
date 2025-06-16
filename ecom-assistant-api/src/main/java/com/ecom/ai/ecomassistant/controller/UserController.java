package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.auth.AuthService;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import com.ecom.ai.ecomassistant.model.dto.mapper.UserMapper;
import com.ecom.ai.ecomassistant.model.dto.request.LoginRequest;
import com.ecom.ai.ecomassistant.model.dto.request.UserCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping
    public User createUser(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        User user = UserMapper.INSTANCE.toUser(userCreateRequest);
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody @Valid LoginRequest loginRequest) {
        return authService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }
}
