package com.ecom.ai.ecomassistant.auth.config;

import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class JwtAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Subject subject = SecurityUtils.getSubject();

        if (subject == null || !subject.isAuthenticated()) {
            return Optional.empty();
        }

        // 根據你的 JWT Filter 實作，這裡的 Principal 可能是 userId、username，或一個自定義的物件
        Object principal = subject.getPrincipal();

        if (principal instanceof String token) {
            return Optional.ofNullable(JwtUtil.getUserId(token));
        } else {
            return Optional.empty();
        }
    }
}
