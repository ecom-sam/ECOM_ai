package com.ecom.ai.ecomassistant.auth.realm;

import com.ecom.ai.ecomassistant.auth.SystemRole;
import com.ecom.ai.ecomassistant.auth.filter.JwtToken;
import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.auth.SystemRoleService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamRoleService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamService;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.lang.util.ByteSource;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouchbaseRealm extends AuthorizingRealm {

    private final UserService userService;
    private final SystemRoleService systemRoleService;
    private final TeamMembershipService teamMembershipService;
    private final TeamService teamService;
    private final TeamRoleService teamRoleService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String jwt = (String) token.getCredentials();

        if (!JwtUtil.validateToken(jwt)) {
            throw new AuthenticationException("Invalid JWT token");
        }

        String userId = JwtUtil.getUserId(jwt);
        if (userId == null || userId.isEmpty()) {
            throw new AuthenticationException("JWT token does not contain userId");
        }

        return new SimpleAuthenticationInfo(jwt, jwt, getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String userId = JwtUtil.getUserId(principals.toString());

        User user = userService.findById(userId).orElseThrow();

        SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        // 添加系統角色和權限
        for (String systemRoleName : user.getSystemRoles()) {
            SystemRole systemRole = SystemRole.fromName(systemRoleName).orElse(null);

            if (systemRole == null) {
                log.warn("systemRole: {} not found.", systemRoleName);
                continue;
            }

            roles.add("system:" + systemRole.name());
            permissions.addAll(systemRole.getPermissionCodes());
        }

        // 添加團隊角色和權限
        for (String membershipId : user.getTeamMembershipIds()) {
            TeamMembership membership = teamMembershipService.findById(membershipId).orElse(null);
            if (membership == null) {
                log.warn("membership: {} not found.", membershipId);
                continue;
            }

            String teamId = membership.getTeamId();
            Team team = teamService.findById(teamId).orElse(null);
            if (team == null) {
                log.warn("team: {} not found.", teamId);
                continue;
            }

            if (!team.isActive()) {
                log.warn("team: {} is not active.", team.getName());
                continue;
            }

            // 當前team管理員
            if (team.getAdminUserId().equals(user.getId())) {
                roles.add("team:" + teamId + ":admin");
                permissions.add("team:" + team.getId() + ":*");
                continue;
            }

            // 當前team其他角色
            for (String teamRoleId : membership.getTeamRoles()) {
                TeamRole teamRole = teamRoleService.findById(teamRoleId).orElse(null);

                if (teamRole == null) {
                    log.warn("teamRole: {} not found.", teamRoleId);
                    continue;
                }

                roles.add("team:" + team.getId() + ":" + teamRole.getName());
                for (String permission : teamRole.getPermissions()) {
                    permissions.add("team:" + team.getId() + ":" + permission);
                }
            }
        }

        authInfo.setRoles(roles);
        authInfo.setStringPermissions(permissions);

        return authInfo;
    }
}
