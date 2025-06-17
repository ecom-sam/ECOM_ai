package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.core.service.TeamManager;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import com.ecom.ai.ecomassistant.db.service.auth.TeamService;
import com.ecom.ai.ecomassistant.model.dto.mapper.TeamRequestMapper;
import com.ecom.ai.ecomassistant.model.dto.request.TeamCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamManager teamManager;

    @GetMapping
    @RequiresPermissions({"system:team:view"})
    public List<Team> search() {
        return teamService.findAll();
    }

    @PostMapping
    @RequiresPermissions({"system:team:manage", "system:team:grant_admin"})
    public Team createTeam(@RequestBody @Valid TeamCreateRequest teamCreateRequest) {
        var command = TeamRequestMapper.INSTANCE.toCreateTeamCommand(teamCreateRequest);
        return teamManager.createTeam(command);
    }
}
