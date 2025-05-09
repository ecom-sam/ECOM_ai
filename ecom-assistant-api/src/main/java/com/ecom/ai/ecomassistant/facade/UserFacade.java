package com.ecom.ai.ecomassistant.facade;

import com.ecom.ai.ecomassistant.db.model.User;
import com.ecom.ai.ecomassistant.db.service.UserService;
import com.ecom.ai.ecomassistant.model.dto.mapper.UserMapper;
import com.ecom.ai.ecomassistant.model.dto.request.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;

    public User createUser(UserCreateRequest userCreateRequest) {
        User user = UserMapper.INSTANCE.toUser(userCreateRequest);
        return userService.createUser(user);
    }
}
