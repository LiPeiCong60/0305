package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Result<String> register(UserDTO userDTO) {
        if (userDTO == null
                || !StringUtils.hasText(userDTO.getUsername())
                || !StringUtils.hasText(userDTO.getPassword())) {
            return Result.error(ResultCode.ERROR);
        }

        User exist = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", userDTO.getUsername()));
        if (exist != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        if (userDTO == null
                || !StringUtils.hasText(userDTO.getUsername())
                || !StringUtils.hasText(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", userDTO.getUsername()));
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        String dbPassword = user.getPassword();
        if (!dbPassword.equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        String token = "Bearer " + UUID.randomUUID();
        return Result.success(token);
    }
}
