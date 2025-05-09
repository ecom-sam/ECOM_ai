package com.ecom.ai.ecomassistant.service;

import com.ecom.ai.ecomassistant.model.User;
import com.ecom.ai.ecomassistant.model.dto.request.UserCreateRequest;
import com.ecom.ai.ecomassistant.model.mapper.UserMapper;
import com.ecom.ai.ecomassistant.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService extends CrudService<User, String, UserRepository> {

    protected UserService(UserRepository repository) {
        super(repository);
    }

    public User createUser(UserCreateRequest userCreateRequest) {
        User user = UserMapper.INSTANCE.toUser(userCreateRequest);
        return repository.save(user);
    }
}
