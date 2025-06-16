package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import com.ecom.ai.ecomassistant.db.repository.auth.TeamRoleRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class TeamRoleService extends CrudService<TeamRole, String, TeamRoleRepository> {
    protected TeamRoleService(TeamRoleRepository repository) {
        super(repository);
    }
}
