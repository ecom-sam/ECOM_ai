package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.core.dto.command.TeamCreateCommand;
import com.ecom.ai.ecomassistant.core.dto.mapper.TeamMapper;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamService;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamManager {

    private final TeamService teamService;
    private final UserService userService;
    private final TeamMembershipService teamMembershipService;

    @Transactional
    public Team createTeam(TeamCreateCommand command) {
        var adminUser = userService
                .findByEmail(command.adminEmail())
                .orElseThrow(() -> new EntityNotFoundException("admin user not found"));

        Team team = TeamMapper.INSTANCE.toTeam(command);
        team.setAdminUserId(adminUser.getId());
        teamService.save(team);

        TeamMembership adminMembership = TeamMembership.builder()
                .teamId(team.getId())
                .userId(adminUser.getId())
                .build();
        teamMembershipService.save(adminMembership);

        adminUser.getTeamMembershipIds().add(adminMembership.getId());
        userService.save(adminUser);

        return team;
    }
}
