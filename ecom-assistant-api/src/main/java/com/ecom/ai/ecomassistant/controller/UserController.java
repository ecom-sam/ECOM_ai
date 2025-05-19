package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.db.model.User;
import com.ecom.ai.ecomassistant.db.service.UserService;
import com.ecom.ai.ecomassistant.model.dto.mapper.UserMapper;
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

    @PostMapping
    public User insert(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        User user = UserMapper.INSTANCE.toUser(userCreateRequest);
        return userService.createUser(user);
    }
}
