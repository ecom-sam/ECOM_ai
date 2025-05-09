package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.User;
import com.ecom.ai.ecomassistant.db.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService extends CrudService<User, String, UserRepository> {

    protected UserService(UserRepository repository) {
        super(repository);
    }

    public User createUser(User user) {
        //checking
        return repository.save(user);
    }
}
