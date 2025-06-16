package com.ecom.ai.ecomassistant.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Objects;

public class JwtUtil {

    private static final String SECRET = "your-256-bit-secret"; // 請改成你的密鑰，務必保密
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    private static final long EXPIRATION = 3600_000; // 1小時

    public static String getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateToken(User user) {
        return generateToken(user, EXPIRATION);
    }

    public static String generateToken(User user, long expireTime) {
        Date date = new Date(System.currentTimeMillis() + expireTime);
        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        return JWT.create()
                .withSubject(user.getId())
                .withArrayClaim("system_roles", user.getSystemRoles().toArray(String[]::new))
                .withExpiresAt(date)
                .sign(algorithm);
    }

    public static boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public static String getTokenFromHeader(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return Objects.nonNull(token) ? token.substring(7) : null;
    }
}
