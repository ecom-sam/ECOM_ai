package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.core.dto.response.LoginResponse;
import com.ecom.ai.ecomassistant.core.dto.response.UserDetailDto;
import com.ecom.ai.ecomassistant.core.service.UserManager;
import com.ecom.ai.ecomassistant.db.model.dto.UserInfo;
import com.ecom.ai.ecomassistant.db.repository.auth.UserRepository;
import com.ecom.ai.ecomassistant.model.dto.mapper.UserRequestMapper;
import com.ecom.ai.ecomassistant.model.dto.request.LoginRequest;
import com.ecom.ai.ecomassistant.model.dto.request.UserActivateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.UserInviteRequest;
import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import com.ecom.ai.ecomassistant.model.dto.response.PageResponse;
import com.ecom.ai.ecomassistant.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManager userManager;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserDetailDto me(@CurrentUserId String userId) {
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
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        return userManager.login(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @GetMapping("/emails")
    @RequiresPermissions({"system:user:list"})
    public List<UserInfo> searchEmail(
            @RequestParam(defaultValue = "") String filter
    ) {
        return userRepository.listEmail(filter);
    }

    @GetMapping
    @RequiresPermissions({"system:user:list"})
    public PageResponse<UserDto> search(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "email") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = PageUtil.buildPageable(page, limit, sortBy, sortDir);
        var pageResult = userManager.search(filter, pageable);
        return PageResponse.of(pageResult);
    }
}
