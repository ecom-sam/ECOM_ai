package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.repository.auth.UserRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
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
