package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.core.dto.command.TeamMembersInviteCommand;
import com.ecom.ai.ecomassistant.core.dto.response.TeamInviteCandidateDto;
import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import com.ecom.ai.ecomassistant.core.exception.EntityExistException;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.model.dto.TeamMemberDto;
import com.ecom.ai.ecomassistant.db.model.dto.UserInfo;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamRoleService;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberManager {

    private final TeamMembershipService teamMembershipService;
    private final TeamRoleService teamRoleService;
    private final UserService userService;
    private final UserManager userManager;

    public List<TeamMemberDto> getTeamMembers(String teamId) {
        return teamMembershipService.findAllByTeamId(teamId);
    }

    public List<TeamMembership> inviteMembers(TeamMembersInviteCommand inviteCommand) {

        String teamId = inviteCommand.teamId();
        Set<String> userIds = Optional.ofNullable(inviteCommand.userIds()).orElse(new HashSet<>());
        Set<String> roleIds = inviteCommand.roleIds();
        Set<String> inviteEmails = Optional.ofNullable(inviteCommand.inviteEmails()).orElse(new HashSet<>());

        if (CollectionUtils.isNotEmpty(inviteEmails)) {
            Set<String> invitedUserIds = userManager
                    .inviteUser(inviteEmails).stream()
                    .map(UserDto::id)
                    .collect(Collectors.toSet());
            userIds.addAll(invitedUserIds);
        }

        if (userIds.isEmpty()) {
            return List.of();
        }

        // check if user is a member
        List<String> existUsers = teamMembershipService
                .findAllByTeamIdAndUserId(teamId, userIds).stream()
                .map(TeamMembership::getUserId)
                .toList();

        if (!existUsers.isEmpty()) {
            throw new EntityExistException("already a team member: " + existUsers);
        }

        // check role
        checkValidTeamRoles(teamId, roleIds);

        // add all membership
        List<TeamMembership> membershipList = new ArrayList<>();
        for (String userId : userIds) {
            TeamMembership membership = TeamMembership.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .teamRoles(roleIds)
                    .build();
            membershipList.add(membership);
        }
        teamMembershipService.saveAll(membershipList);

        return membershipList;
    }

    public List<TeamInviteCandidateDto> searchInviteCandidates(String teamId, String filter, int limit) {
        // 1. 找出 email/name 吻合的使用者
        List<User> users = userService.search(filter, limit);

        // 查出此 team 已有的 userId 清單
        Set<String> memberUserIds = teamMembershipService.findAllByTeamId(teamId)
                .stream()
                .map(TeamMemberDto::getUser)
                .map(UserInfo::getId)
                .collect(Collectors.toSet());

        return users.stream().map(user -> new TeamInviteCandidateDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                memberUserIds.contains(user.getId())
        )).collect(Collectors.toList());
    }

    protected void checkValidTeamRoles(String teamId, Set<String> roleIds) {
        // check role
        List<String> validRoleIds = teamRoleService
                .findAllById(roleIds).stream()
                .filter(r -> r.getIsSystemRole() || Objects.equals(r.getTeamId(), teamId))
                .map(TeamRole::getId)
                .toList();

        List<String> invalidRoleIds = roleIds.stream()
                .filter(id -> !validRoleIds.contains(id))
                .toList();

        if (!invalidRoleIds.isEmpty()) {
            throw new EntityNotFoundException("role not found: " + invalidRoleIds);
        }
    }

    public void removeMember(String teamId, String userId) {
        var teamMembership = teamMembershipService
                .findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new EntityNotFoundException("teamMembership not found."));
        teamMembershipService.delete(teamMembership);
    }

    public void updateTeamMemberRoles(String teamId, String userId, Set<String> roleIds) {
        checkValidTeamRoles(teamId, roleIds);

        TeamMembership membership = teamMembershipService
                .findByTeamIdAndUserId(teamId, userId)
                .orElse(
                        TeamMembership.builder()
                                .teamId(teamId)
                                .userId(userId)
                                .build()
                );

        membership.setTeamRoles(roleIds);

        teamMembershipService.save(membership);
    }
}
