package com.ecom.ai.ecomassistant.auth.filter;

import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import com.ecom.ai.ecomassistant.auth.util.ResponseUtil;
import com.ecom.ai.ecomassistant.common.dto.ErrorResponse;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtFilter extends BearerHttpAuthenticationFilter {

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader("Authorization");
        return authorization != null;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        String token = JwtUtil.getTokenFromHeader((HttpServletRequest) request);

        JwtToken jwt = new JwtToken(token);
        getSubject(request, response).login(jwt);
        return true;
    }

    @SneakyThrows
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginAttempt(request, response)) {
            try {
                return executeLogin(request, response);
            } catch (AuthenticationException e) {
                log.warn("JWT authentication failed: {}", e.getMessage());
                response401(response, e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error during login", e);
                response401(response, "Internal error during authentication");
            }
        }
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        response401(response);
        return false;
    }

    private void response401(ServletResponse response) throws IOException {
        response401(response, "Token is missing or invalid");
    }

    @SneakyThrows
    private void response401(ServletResponse response, String message) throws IOException {
        var error = new ErrorResponse<>(
                401,
                "Unauthorized",
                List.of(message)
        );
        ResponseUtil.writeJson((HttpServletResponse) response, 401, error);
    }


    // CORS
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}
