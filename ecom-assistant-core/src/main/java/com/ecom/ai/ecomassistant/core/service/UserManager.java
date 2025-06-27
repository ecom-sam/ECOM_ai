package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.auth.SystemRole;
import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import com.ecom.ai.ecomassistant.common.UserStatus;
import com.ecom.ai.ecomassistant.core.dto.command.UserActivateCommand;
import com.ecom.ai.ecomassistant.core.dto.mapper.UserMapper;
import com.ecom.ai.ecomassistant.core.dto.response.LoginResponse;
import com.ecom.ai.ecomassistant.core.dto.response.UserDetailDto;
import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import com.ecom.ai.ecomassistant.core.exception.EntityExistException;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamRoleService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamService;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManager {

    private final UserService userService;
    private final TeamService teamService;
    private final TeamMembershipService teamMembershipService;
    private final TeamRoleService teamRoleService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDetailDto getUserDetail(String id) {
        User user = getUserById(id);
        return UserMapper.INSTANCE.toUserDetailDto(user);
    }

    public UserDto inviteUser(String email) {
        if (isUserExist(email)) {
            throw new EntityExistException("user exist");
        }

        User user = new User();
        user.setEmail(email);
        user.setSystemRoles(Set.of(SystemRole.USER.name()));
        userService.save(user);

        return UserMapper.INSTANCE.toDto(user);
    }

    public UserDto activateUser(UserActivateCommand command) {
        //check token
        //command.token();

        User user = getUserById(command.id());
        user.setName(command.name());
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setStatus(UserStatus.ACTIVE);
        userService.save(user);

        return UserMapper.INSTANCE.toDto(user);
    }


    public boolean isUserExist(String email) {
        return userService.findByEmail(email).isPresent();
    }

    public LoginResponse login(String email, String password) {
        User user = getUserByEmail(email);

        boolean isCorrectPassword = passwordEncoder.matches(password, user.getPassword());
        if (!isCorrectPassword) {
            throw new UnauthenticatedException("login failed");
        }

        UserRoleContext userRoleContext = getUserRoleContext(user);

        return new LoginResponse(
                JwtUtil.generateToken(user),
                UserMapper.INSTANCE.toPermissionDto(user, userRoleContext)
        );
    }


    public Page<UserDto> search(String filter, Pageable pageable) {
        return userService
                .searchByCriteria(filter, pageable)
                .map(UserMapper.INSTANCE::toDto);
    }

    protected User getUserById(String id) {
        return userService
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    protected User getUserByEmail(String email) {
        return userService
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public record UserRoleContext(
            Set<String> roles,
            Set<String> permissions
    ) {}

    public UserRoleContext getUserRoleContext(User user) {
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

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
        List<TeamMembership> userTeamMemberships = teamMembershipService.findAllByUserId(user.getId());
        for (var membership : userTeamMemberships) {

            String teamId = membership.getTeamId();
            Team team = teamService.findById(teamId).orElse(null);
            if (team == null) {
                log.warn("team: {} not found.", teamId);
                continue;
            }

            // 當前team管理員
            if (Objects.equals(team.getOwnerId(), user.getId())) {
                permissions.add("team:" + team.getId() + ":*");
                continue;
            }

            // 當前team其他角色
            Set<String> teamRoles = Optional.ofNullable(membership.getTeamRoles()).orElse(new HashSet<>());
            for (String teamRoleId : teamRoles) {
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

        return new UserRoleContext(roles, permissions);
    }

}
