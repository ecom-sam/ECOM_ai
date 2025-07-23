package com.ecom.ai.ecomassistant.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeJson(HttpServletResponse response, int statusCode, Object body) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
