package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.core.dto.DtoUtil;
import com.ecom.ai.ecomassistant.core.dto.command.TeamCreateCommand;
import com.ecom.ai.ecomassistant.core.dto.mapper.TeamMapper;
import com.ecom.ai.ecomassistant.db.model.dto.TeamDetailDto;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamManager {

    private final TeamService teamService;
    private final UserService userService;
    private final TeamMembershipService teamMembershipService;

    public List<TeamListDto> list(String userId) {
        // 查使用者 + 所屬的 teamIds（做 isMember 判斷用）
        UserTeamContext context = getUserTeamContext(userId);
        Set<String> userTeamIds = context.teamMembershipMap().keySet();

        // 判斷權限 - 檢查系統級權限或任何團隊級權限
        Subject subject = SecurityUtils.getSubject();
        boolean hasSystemTeamPermission = subject.isPermitted("system:team:*") || 
                                         subject.isPermitted("system:team:view") ||
                                         subject.isPermitted("system:*");
        
        log.debug("User {} system team permissions: system:team:* = {}, system:team:view = {}, system:* = {}", 
                userId, 
                subject.isPermitted("system:team:*"),
                subject.isPermitted("system:team:view"),
                subject.isPermitted("system:*"));
        
        // 檢查是否有任何團隊的檢視權限
        // 支援兩種權限格式：
        // 1. team:{teamId}:view (標準格式)
        // 2. team:view (自定義角色格式)
        boolean hasAnyTeamPermission = false;
        for (String teamId : userTeamIds) {
            boolean teamSpecific = subject.isPermitted("team:" + teamId + ":view") || subject.isPermitted("team:" + teamId + ":*");
            boolean generic = subject.isPermitted("team:view") || subject.isPermitted("team:*");
            log.debug("User {} team permissions for {}: team:{}:view = {}, team:{}:* = {}, team:view = {}, team:* = {}", 
                    userId, teamId, teamId, 
                    subject.isPermitted("team:" + teamId + ":view"),
                    teamId,
                    subject.isPermitted("team:" + teamId + ":*"),
                    subject.isPermitted("team:view"), 
                    subject.isPermitted("team:*"));
            if (teamSpecific || generic) {
                hasAnyTeamPermission = true;
                break;
            }
        }
        
        log.info("User {} permission check result: hasSystemTeamPermission = {}, hasAnyTeamPermission = {}", 
                userId, hasSystemTeamPermission, hasAnyTeamPermission);
        
        List<Team> teams = (hasSystemTeamPermission || hasAnyTeamPermission)
                ? teamService.findAllWithSort()
                : context.teams(); // 無權限時，只能看自己加入的團隊
                
        log.info("User {} will see {} teams (all teams: {}, context teams: {})", 
                userId, teams.size(), 
                hasSystemTeamPermission || hasAnyTeamPermission ? "YES" : "NO",
                context.teams().size());

        // 抓出所有 teamId 以查人數
        Set<String> teamIds = teams.stream()
                .map(Team::getId)
                .collect(Collectors.toSet());

        // 統計 team 成員數
        Map<String, Integer> userCountMap = teamMembershipService
                .countGroupedByTeamId(teamIds).stream()
                .collect(Collectors.toMap(TeamUserCount::getTeamId, TeamUserCount::getCount));

        // 建立 DTO 清單
        return teams.stream()
                .map(team -> TeamMapper.INSTANCE.toListDto(
                        team,
                        userId.equals(team.getOwnerId()),               // isOwner
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

        Map<String, TeamMembership> membershipMap = teamMembershipService
                .findAllByUserId(userId).stream()
                .collect(Collectors.toMap(TeamMembership::getTeamId, tm -> tm));

        List<Team> teams = teamService.findAllByIdWithSort(membershipMap.keySet());

        return new UserTeamContext(membershipMap, teams);
    }

    /***
     * create team and assign owner
     * @return team entity
     */
    public Team createTeam(TeamCreateCommand command) {
        var teamOwner = userService
                .findByEmail(command.ownerEmail())
                .orElseThrow(() -> new EntityNotFoundException("admin user not found"));

        Team team = TeamMapper.INSTANCE.toTeam(command);
        team.setOwnerId(teamOwner.getId());
        teamService.save(team);

        TeamMembership adminMembership = TeamMembership.builder()
                .teamId(team.getId())
                .userId(teamOwner.getId())
                .build();
        teamMembershipService.save(adminMembership);

        return team;
    }

    public TeamDetailDto getTeamDetail(String teamId) {
        Team team = getTeam(teamId);
        TeamDetailDto dto = TeamMapper.INSTANCE.toDetailDto(team);

        Set<String> userIds = Stream.of(dto.getOwnerId(), dto.getCreatedBy(), dto.getLastModifiedBy())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, User> userMap = userService
                .findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        DtoUtil.setUserName(userMap, dto::getOwnerId, dto::setOwnerName);
        DtoUtil.setUserName(userMap, dto::getCreatedBy, dto::setCreatedByDisplayName);
        DtoUtil.setUserName(userMap, dto::getLastModifiedBy, dto::setLastModifiedByDisplayName);

        return dto;
    }

    public Team getTeam(String teamId) {
        return teamService
                .findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
    }
}
