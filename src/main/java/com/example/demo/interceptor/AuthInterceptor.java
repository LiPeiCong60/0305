package com.example.demo.interceptor;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 放行登录接口: POST /api/users/login
        if ("POST".equalsIgnoreCase(method) && "/api/users/login".equals(uri)) {
            return true;
        }

        // 放行注册接口: POST /api/users
        if ("POST".equalsIgnoreCase(method) && "/api/users".equals(uri)) {
            return true;
        }

        // 其余接口检查 Authorization
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Result<Object> result = Result.error(ResultCode.TOKEN_INVALID);
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        return true;
    }
}
