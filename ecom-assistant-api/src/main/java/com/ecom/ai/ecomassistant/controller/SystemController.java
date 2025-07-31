package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.core.dto.mapper.TeamRoleMapper;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleDto;
import com.ecom.ai.ecomassistant.db.service.auth.TeamRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final TeamRoleService teamRoleService;

    @GetMapping("/team-role-templates")
    @RequiresPermissions({"system:*", "system:team:*"})
    public List<TeamRoleDto> getSystemTeamRoles() {
        return teamRoleService
                .findSystemTeamRole().stream()
                .map(TeamRoleMapper.INSTANCE::toDto)
                .toList();
    }


}
