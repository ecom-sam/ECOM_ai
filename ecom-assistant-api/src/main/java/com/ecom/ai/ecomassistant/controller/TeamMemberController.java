package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.auth.util.PermissionUtil;
import com.ecom.ai.ecomassistant.core.dto.response.TeamInviteCandidateDto;
import com.ecom.ai.ecomassistant.core.service.TeamMemberManager;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.dto.TeamMemberDto;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import com.ecom.ai.ecomassistant.model.dto.mapper.TeamMemberRequestMapper;
import com.ecom.ai.ecomassistant.model.dto.request.TeamMemberRemoveRequest;
import com.ecom.ai.ecomassistant.model.dto.request.TeamMemberRoleUpdateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.TeamMembersInviteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.ecom.ai.ecomassistant.auth.permission.SystemPermission.SYSTEM_TEAM_MANAGE;
import static com.ecom.ai.ecomassistant.auth.permission.TeamPermission.TEAM_MEMBERS_MANAGE;
import static com.ecom.ai.ecomassistant.auth.permission.TeamPermission.TEAM_MEMBERS_VIEW;
import static com.ecom.ai.ecomassistant.auth.permission.TeamPermission.TEAM_MEMBERS_INVITE;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberManager teamMemberManager;
    private final TeamMembershipService teamMembershipService;

    @GetMapping
    public List<TeamMemberDto> teamMembers(@PathVariable String teamId) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_MEMBERS_VIEW.getCodeWithTeamId(teamId)
        ));
        return teamMemberManager.getTeamMembers(teamId);
    }

    @PostMapping("/invitations")
    public List<TeamMembership> inviteMembers(
            @PathVariable String teamId,
            @RequestBody @Valid TeamMembersInviteRequest inviteRequest
    ) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_MEMBERS_INVITE.getCodeWithTeamId(teamId)
        ));
        var command = TeamMemberRequestMapper.INSTANCE.toInviteCommand(inviteRequest, teamId);
        return teamMemberManager.inviteMembers(command);
    }

    @GetMapping("/invite-candidates")
    public List<TeamInviteCandidateDto> searchInviteCandidates(
            @PathVariable String teamId,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(required = false, defaultValue = "20") int limit
    ) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_MEMBERS_INVITE.getCodeWithTeamId(teamId)
        ));
        return teamMemberManager.searchInviteCandidates(teamId, filter, limit);
    }

    @PatchMapping("/{userId}/roles")
    public List<TeamMemberDto> updateMemberRole(
            @PathVariable String teamId,
            @PathVariable String userId,
            @RequestBody @Valid TeamMemberRoleUpdateRequest updateRequest
    ) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_MEMBERS_MANAGE.getCodeWithTeamId(teamId)
        ));
        teamMemberManager.updateTeamMemberRoles(teamId, userId, updateRequest.roles());
        return teamMembershipService.findDtoByTeamIdAndUserId(teamId, userId);
    }


    @DeleteMapping("/{userId}")
    public void removeMembers(
            @PathVariable String teamId,
            @PathVariable String userId
    ) {
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_TEAM_MANAGE.getCode(),
                TEAM_MEMBERS_MANAGE.getCodeWithTeamId(teamId)
        ));
        teamMemberManager.removeMember(teamId, userId);
    }


}
