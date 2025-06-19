package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.core.dto.command.TeamCreateCommand;
import com.ecom.ai.ecomassistant.core.dto.mapper.TeamMapper;
import com.ecom.ai.ecomassistant.core.dto.response.TeamListDto;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.model.dto.TeamUserCount;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamService;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamManager {

    private final TeamService teamService;
    private final UserService userService;
    private final TeamMembershipService teamMembershipService;

    public List<TeamListDto> list(String userId) {
        // 查使用者 + 所屬的 teamIds（做 isMember 判斷用）
        UserTeamContext context = getUserTeamContext(userId);
        Set<String> userTeamIds = context.teamMembershipMap().keySet();

        // 判斷權限
        Subject subject = SecurityUtils.getSubject();
        List<Team> teams = subject.isPermitted("system:team:view")
                ? teamService.findAll()
                : context.teams(); // 無權限時，只能看自己加入的團隊

        // 抓出所有 teamId 以查人數
        Set<String> teamIds = teams.stream()
                .map(Team::getId)
                .collect(Collectors.toSet());

        // 統計 team 成員數
        Map<String, Integer> userCountMap = teamMembershipService
                .countGroupedByTeamId(teamIds).stream()
                .collect(Collectors.toMap(TeamUserCount::teamId, TeamUserCount::count));

        // 建立 DTO 清單
        return teams.stream()
                .map(team -> TeamMapper.INSTANCE.toListDto(
                        team,
                        userId.equals(team.getAdminUserId()),               // isOwner
                        userTeamIds.contains(team.getId()),                // isMember
                        userCountMap.getOrDefault(team.getId(), 0)         // userCount
                ))
                .toList();
    }

    public record UserTeamContext(
            Map<String, TeamMembership> teamMembershipMap,
            List<Team> teams
    ) {}

    private UserTeamContext getUserTeamContext(String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        Set<String> membershipIds = user.getTeamMembershipIds();
        if (membershipIds == null || membershipIds.isEmpty()) {
            return new UserTeamContext(Collections.emptyMap(), Collections.emptyList());
        }

        List<TeamMembership> memberships = teamMembershipService.findAllById(membershipIds);
        Map<String, TeamMembership> membershipMap = memberships.stream()
                .collect(Collectors.toMap(TeamMembership::getTeamId, tm -> tm));

        List<Team> teams = teamService.findAllById(membershipMap.keySet());
        return new UserTeamContext(membershipMap, teams);
    }

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
