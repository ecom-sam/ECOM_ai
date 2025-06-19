package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.dto.TeamUserCount;
import com.ecom.ai.ecomassistant.db.repository.auth.TeamMembershipRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TeamMembershipService extends CrudService<TeamMembership, String, TeamMembershipRepository> {
    protected TeamMembershipService(TeamMembershipRepository repository) {
        super(repository);
    }

    public List<TeamMembership> findAllById(Set<String> ids) {
        return repository.findAllById(ids);
    }

    public List<TeamUserCount> countGroupedByTeamId(Set<String> teamIds) {
        return repository.countGroupedByTeamId(teamIds);
    }
}
