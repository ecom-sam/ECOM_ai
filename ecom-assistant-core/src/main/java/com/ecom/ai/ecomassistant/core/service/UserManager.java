package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.auth.SystemRole;
import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import com.ecom.ai.ecomassistant.core.dto.command.UserActivateCommand;
import com.ecom.ai.ecomassistant.core.dto.mapper.UserMapper;
import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import com.ecom.ai.ecomassistant.core.exception.EntityExistException;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserManager {

    private final UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDto getUserDetail(String id) {
        User user = getUserById(id);
        return UserMapper.INSTANCE.toDto(user);
    }

    public UserDto inviteUser(String email) {
        if (isUserExist(email)) {
            throw new EntityExistException("user exist");
        }

        User user = new User();
        user.setEmail(email);
        user.setSystemRoles(Set.of(SystemRole.USER.name()));
        userService.save(user);

        return UserMapper.INSTANCE.toDto(user);
    }

    public UserDto activateUser(UserActivateCommand command) {
        //check token
        //command.token();

        User user = getUserById(command.id());
        user.setName(command.name());
        user.setPassword(passwordEncoder.encode(command.password()));
        userService.save(user);

        return UserMapper.INSTANCE.toDto(user);
    }


    public boolean isUserExist(String email) {
        return userService.findByEmail(email).isPresent();
    }

    public String login(String email, String password) {
        User user = getUserByEmail(email);

        boolean isCorrectPassword = passwordEncoder.matches(password, user.getPassword());
        if (!isCorrectPassword) {
            throw new UnauthenticatedException("wrong password");
        }

        // 登入成功產生token
        return JwtUtil.generateToken(user);
    }

    protected User getUserById(String id) {
        return userService
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    protected User getUserByEmail(String email) {
        return userService
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

}
