package com.ecom.ai.ecomassistant.auth;

import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public String login(String email, String password) {
        // 先驗證帳密，這裡簡化省略
        User user = userService
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 登入成功產生token
        return JwtUtil.generateToken(user);
    }
}
