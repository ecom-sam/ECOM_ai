package com.ecom.ai.ecom_assistant.service;

import com.ecom.ai.ecom_assistant.model.User;
import com.ecom.ai.ecom_assistant.model.dto.request.UserCreateRequest;
import com.ecom.ai.ecom_assistant.model.mapper.UserMapper;
import com.ecom.ai.ecom_assistant.repository.UserRepository;
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
