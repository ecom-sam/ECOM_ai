package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.repository.auth.UserRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService extends CrudService<User, String, UserRepository> {

    protected UserService(UserRepository repository) {
        super(repository);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Page<User> searchByCriteria(String filter, Pageable pageable) {
        return repository.searchByCriteria(filter, pageable);
    }
}
