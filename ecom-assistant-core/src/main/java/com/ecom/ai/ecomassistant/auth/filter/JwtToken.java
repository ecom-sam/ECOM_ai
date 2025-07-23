package com.ecom.ai.ecomassistant.auth.filter;

import lombok.AllArgsConstructor;
import org.apache.shiro.authc.AuthenticationToken;

@AllArgsConstructor
public class JwtToken implements AuthenticationToken {

    private String token;

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
