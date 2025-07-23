package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.dto.TeamMemberDto;
import com.ecom.ai.ecomassistant.db.model.dto.TeamUserCount;
import com.ecom.ai.ecomassistant.db.repository.auth.TeamMembershipRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TeamMembershipService extends CrudService<TeamMembership, String, TeamMembershipRepository> {
    protected TeamMembershipService(TeamMembershipRepository repository) {
        super(repository);
    }

    public List<TeamMembership> findAllById(Set<String> ids) {
        return repository.findAllById(ids);
    }

    public List<TeamMembership> findAllByUserId(String ids) {
        return repository.findAllByUserId(ids);
    }

    public List<TeamUserCount> countGroupedByTeamId(Set<String> teamIds) {
        return repository.countGroupedByTeamId(teamIds);
    }

    public List<TeamMemberDto> findDtoByTeamIdAndUserId(String teamId, String userId) {
        return repository.findDtoByTeamIdAndUserId(teamId, userId);
    }

    public List<TeamMemberDto> findAllByTeamId(String teamId) {
        return repository.findAllDtoByTeamId(teamId);
    }

    public Optional<TeamMembership> findByTeamIdAndUserId(String teamId, String userId) {
        return repository.findByTeamIdAndUserId(teamId, userId);
    }

    public List<TeamMembership> findAllByTeamIdAndUserId(String teamId, Set<String> userIds) {
        return repository.findAllByTeamIdAndUserIdIn(teamId, userIds.stream().toList());
    }

    public List<TeamMembership> findAllContainsTeamRole(String teamRoleId) {
        return repository.findAllContainsTeamRole(teamRoleId);
    }

    public void delete(TeamMembership membership) {
        repository.delete(membership);
    }

    public void deleteAll(List<TeamMembership> memberships) {
        repository.deleteAll(memberships);
    }
}
