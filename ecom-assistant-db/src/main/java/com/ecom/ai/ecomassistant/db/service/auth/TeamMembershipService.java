package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.repository.auth.TeamMembershipRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class TeamMembershipService extends CrudService<TeamMembership, String, TeamMembershipRepository> {
    protected TeamMembershipService(TeamMembershipRepository repository) {
        super(repository);
    }
}
