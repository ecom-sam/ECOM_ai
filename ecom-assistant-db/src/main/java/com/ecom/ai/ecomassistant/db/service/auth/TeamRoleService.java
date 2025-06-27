package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleDto;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleUserCountDto;
import com.ecom.ai.ecomassistant.db.repository.auth.TeamRoleRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamRoleService extends CrudService<TeamRole, String, TeamRoleRepository> {
    protected TeamRoleService(TeamRoleRepository repository) {
        super(repository);
    }

    public List<TeamRole> findAllByTeamId(String teamId) {
        return repository.findAllByTeamId(teamId);
    }

    public List<TeamRole> findSystemTeamRole() {
        return repository.findByIsSystemRoleTrue();
    }

    public List<TeamRoleUserCountDto> countUsersByRoleIds(List<String> roleIds) {
        return repository.countUsersByRoleIds(roleIds);
    }

    public List<TeamRoleDto> findSystemTeamRoleByTeamId(String teamId) {
        return repository.findSystemTeamRoleByTeamId(teamId);
    }

    public int countRoles(String teamId, List<String> roleIds) {
        return repository.countRoles(teamId, roleIds);
    }

    public void delete(String roleId) {
        repository.deleteById(roleId);
    }

}
