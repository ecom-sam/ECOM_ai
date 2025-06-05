package com.ecom.ai.ecomassistant.auth.filter;

import com.ecom.ai.ecomassistant.auth.util.ResponseUtil;
import com.ecom.ai.ecomassistant.common.dto.ErrorResponse;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;

import java.util.List;

@Slf4j
public class JwtFilter extends BearerHttpAuthenticationFilter {

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        var error = new ErrorResponse<>(
                401,
                "Unauthorized",
                List.of("Token is missing or invalid")
        );

        ResponseUtil.writeJson((HttpServletResponse) response, 401, error);
        return false;
    }
}
