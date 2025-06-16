package com.ecom.ai.ecomassistant.auth;

import com.ecom.ai.ecomassistant.auth.util.JwtUtil;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    public String login(String email, String password) {
        // 先驗證帳密，這裡簡化省略
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setPassword(password);

        String role = email.split("@")[0];
        user.setSystemRoles(Set.of(role));

        // 登入成功產生token
        return JwtUtil.generateToken(user);
    }
}
