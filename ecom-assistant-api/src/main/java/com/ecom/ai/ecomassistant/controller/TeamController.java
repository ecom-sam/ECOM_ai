package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.db.model.dto.TeamDetailDto;
import com.ecom.ai.ecomassistant.core.dto.response.TeamListDto;
import com.ecom.ai.ecomassistant.core.service.TeamManager;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import com.ecom.ai.ecomassistant.model.dto.mapper.TeamRequestMapper;
import com.ecom.ai.ecomassistant.model.dto.request.TeamCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamManager teamManager;

    public record TeamListGroupDto(List<TeamListDto> myTeams, List<TeamListDto> managedTeams) { }

    @GetMapping
    public TeamListGroupDto list(@CurrentUserId String currentUserId) {
        List<TeamListDto> allTeams = teamManager.list(currentUserId);

        Map<Boolean, List<TeamListDto>> partitioned = allTeams.stream()
                .collect(Collectors.partitioningBy(TeamListDto::isMember));

        return new TeamListGroupDto(
                partitioned.get(true),  // my teams
                partitioned.get(false)  // managed teams
        );
    }

    @PostMapping
    @RequiresPermissions({"system:team:manage", "system:team:grant_admin"})
    public Team createTeam(@RequestBody @Valid TeamCreateRequest teamCreateRequest) {
        var command = TeamRequestMapper.INSTANCE.toCreateTeamCommand(teamCreateRequest);
        return teamManager.createTeam(command);
    }

    @GetMapping("/{teamId}")
    public TeamDetailDto teamDetail(@PathVariable String teamId) {
        return teamManager.getTeamDetail(teamId);
    }
}
