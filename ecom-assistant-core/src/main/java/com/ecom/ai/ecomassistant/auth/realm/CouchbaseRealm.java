package com.ecom.ai.ecomassistant.auth.realm;

import com.ecom.ai.ecomassistant.auth.filter.JwtToken;
import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import com.ecom.ai.ecomassistant.core.service.UserManager;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouchbaseRealm extends AuthorizingRealm {

    private final UserService userService;
    private final UserManager userManager;

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
        // 從 principal 中取得 JWT token
        String jwt = (String) principals.getPrimaryPrincipal();
        String userId = JwtUtil.getUserId(jwt);
        
        log.debug("Getting authorization info for user: {}", userId);

        User user = userService.findById(userId).orElseThrow();

        SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();
        UserManager.UserRoleContext userRoleContext = userManager.getUserRoleContext(user);

        log.debug("User {} has roles: {} and permissions: {}", userId, userRoleContext.roles(), userRoleContext.permissions());
        
        authInfo.setRoles(userRoleContext.roles());
        authInfo.setStringPermissions(userRoleContext.permissions());

        return authInfo;
    }
}
