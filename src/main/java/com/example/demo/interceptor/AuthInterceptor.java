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

        // 1. 登录接口放行
        if ("POST".equalsIgnoreCase(method) && "/api/users/login".equals(uri)) {
            return true;
        }

        // 2. 按 HTTP 动词精细放行
        // 放行：POST /api/users（注册）
        boolean isCreateUser = "POST".equalsIgnoreCase(method) && "/api/users".equals(uri);

        if (isCreateUser) {
            return true;
        }

        // 3. 其余接口必须校验 Authorization
        String token = request.getHeader("Authorization");

        if (token == null || token.trim().isEmpty()) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");

            response.setStatus(HttpServletResponse.SC_OK);

            Result<Object> result = Result.error(ResultCode.TOKEN_INVALID);
            String json = objectMapper.writeValueAsString(result);

            response.getWriter().write(json);
            return false;
        }

        // 4. 有 token，放行
        return true;
    }
}
