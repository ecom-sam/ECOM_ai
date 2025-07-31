package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.auth.util.PermissionUtil;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleDto;
import com.ecom.ai.ecomassistant.core.service.TeamRoleManager;
import com.ecom.ai.ecomassistant.model.dto.mapper.TeamRoleRequestMapper;
import com.ecom.ai.ecomassistant.model.dto.request.TeamRoleCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.TeamRoleUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.ecom.ai.ecomassistant.auth.permission.SystemPermission.SYSTEM_TEAM_MANAGE;
import static com.ecom.ai.ecomassistant.auth.permission.TeamPermission.TEAM_ROLES_MANAGE;
import static com.ecom.ai.ecomassistant.auth.permission.TeamPermission.TEAM_ROLES_VIEW;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/roles")
@RequiredArgsConstructor
public class TeamRoleController {

    private final TeamRoleManager teamRoleManager;

    /*
     * 查詢某 Team 所有角色
     */
    @GetMapping
    public TeamRoleManager.TeamRolesResponse getRoles(@PathVariable String teamId) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_ROLES_VIEW.getCodeWithTeamId(teamId)
        ));
        return teamRoleManager.getRoles(teamId);
    }

    @PostMapping
    public TeamRoleDto createTeamRole(@PathVariable String teamId, @RequestBody @Valid TeamRoleCreateRequest request) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_ROLES_MANAGE.getCodeWithTeamId(teamId)
        ));
        var command = TeamRoleRequestMapper.INSTANCE.toCreateTeamRoleCommand(request, teamId);
        return teamRoleManager.createTeamRole(command);
    }

    @GetMapping("/{roleId}")
    public TeamRoleDto getTeamRole(@PathVariable String teamId, @PathVariable String roleId) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_ROLES_VIEW.getCodeWithTeamId(teamId)
        ));
        return teamRoleManager.getTeamRole(roleId);
    }

    @PatchMapping("/{roleId}")
    public TeamRoleDto updateTeamRole(
            @PathVariable String teamId,
            @PathVariable String roleId,
            @RequestBody @Valid TeamRoleUpdateRequest updateRequest
    ) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_ROLES_MANAGE.getCodeWithTeamId(teamId)
        ));
        var command = TeamRoleRequestMapper.INSTANCE.toTeamRoleUpdateCommand(updateRequest, teamId);
        return teamRoleManager.updateTeamRole(roleId, command);
    }

    @DeleteMapping("/{roleId}")
    public void deleteTeamRole(@PathVariable String teamId, @PathVariable String roleId) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_ROLES_MANAGE.getCodeWithTeamId(teamId)
        ));
        teamRoleManager.deleteTeamRole(roleId);
    }
}
