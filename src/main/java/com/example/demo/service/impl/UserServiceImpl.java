package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    // 本节先用内存 Map 模拟数据库，后续可替换为 UserMapper 持久化。
    private static final Map<String, String> USER_DB = new ConcurrentHashMap<>();

    @Override
    public Result<String> register(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUsername() == null || userDTO.getPassword() == null) {
            return Result.error(ResultCode.ERROR);
        }

        if (USER_DB.containsKey(userDTO.getUsername())) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        USER_DB.put(userDTO.getUsername(), userDTO.getPassword());
        return Result.success("注册成功");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUsername() == null || userDTO.getPassword() == null) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        if (!USER_DB.containsKey(userDTO.getUsername())) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        String dbPassword = USER_DB.get(userDTO.getUsername());
        if (!dbPassword.equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        String token = "Bearer " + UUID.randomUUID();
        return Result.success(token);
    }
}
