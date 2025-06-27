package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.SystemRole;
import com.ecom.ai.ecomassistant.db.repository.auth.SystemRoleRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class SystemRoleService extends CrudService<SystemRole, String, SystemRoleRepository> {

    protected SystemRoleService(SystemRoleRepository repository) {
        super(repository);
    }
}
