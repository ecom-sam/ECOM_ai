package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.core.dto.command.TeamRoleCreateCommand;
import com.ecom.ai.ecomassistant.core.dto.command.TeamRoleUpdateCommand;
import com.ecom.ai.ecomassistant.core.dto.mapper.TeamRoleMapper;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleDto;
import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleUserCountDto;
import com.ecom.ai.ecomassistant.db.service.auth.TeamRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamRoleManager {

    private final TeamRoleService teamRoleService;

    public record TeamRolesResponse(
            List<TeamRoleDto> system,
            List<TeamRoleDto> team
    ) {}


    public TeamRolesResponse getRoles(String teamId) {
        return new TeamRolesResponse(
                getSystemTeamRole(teamId),
                getTeamRoles(teamId)
        );
    }

    public List<TeamRoleDto> getSystemTeamRole(String teamId) {
        return teamRoleService.findSystemTeamRoleByTeamId(teamId);
    }

    public List<TeamRoleDto> getTeamRoles(String teamId) {
        List<TeamRole> teamRoles = teamRoleService
                .findAllByTeamId(teamId);

        List<String> roleIds = teamRoles.stream()
                .map(TeamRole::getId)
                .toList();

        Map<String, Integer> roleUserCountMap = teamRoleService
                .countUsersByRoleIds(roleIds).stream()
                .collect(Collectors.toMap(TeamRoleUserCountDto::roleId, TeamRoleUserCountDto::userCount));

        return teamRoles.stream()
                .map(role -> {
                    Integer userCount = roleUserCountMap.getOrDefault(role.getId(), 0);
                    return TeamRoleMapper.INSTANCE.toDto(role, userCount);
                })
                .toList();
    }

    public TeamRoleDto createTeamRole(TeamRoleCreateCommand command) {
        var role = TeamRoleMapper.INSTANCE.toTeamRole(command);
        teamRoleService.save(role);
        return TeamRoleMapper.INSTANCE.toDto(role);
    }

    public TeamRoleDto getTeamRole(String roleId) {
        TeamRole role = getRole(roleId);
        return TeamRoleMapper.INSTANCE.toDto(role);
    }

    //TODO update api permission
    public TeamRoleDto updateTeamRole(String roleId, TeamRoleUpdateCommand command) {
        TeamRole role = getRole(roleId);
        TeamRole newRole = TeamRoleMapper.INSTANCE.toTeamRole(role, command);
        teamRoleService.save(newRole);
        return TeamRoleMapper.INSTANCE.toDto(newRole);
    }

    public void deleteTeamRole(String roleId) {
        //TODO check can delete, is used...?
        teamRoleService.delete(roleId);
    }

    protected TeamRole getRole(String roleId) {
        return teamRoleService
                .findById(roleId)
                .orElseThrow(() -> new  EntityNotFoundException("role not found: " + roleId));
    }
}
