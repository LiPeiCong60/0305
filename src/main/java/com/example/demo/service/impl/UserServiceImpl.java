package com.example.demo.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserInfo;
import com.example.demo.mapper.UserInfoMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String CACHE_KEY_PREFIX = "user:detail:";
    private static final long CACHE_TTL_MINUTES = 10L;

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper,
                           UserInfoMapper userInfoMapper,
                           StringRedisTemplate redisTemplate,
                           JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.userInfoMapper = userInfoMapper;
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Result<String> register(UserDTO userDTO) {
        if (userDTO == null
                || !StringUtils.hasText(userDTO.getUsername())
                || !StringUtils.hasText(userDTO.getPassword())) {
            return Result.error(ResultCode.ERROR);
        }

        User exist = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", userDTO.getUsername()));
        if (exist != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        userMapper.insert(user);
        return Result.success("\u6ce8\u518c\u6210\u529f");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        if (userDTO == null
                || !StringUtils.hasText(userDTO.getUsername())
                || !StringUtils.hasText(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", userDTO.getUsername()));
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        if (!user.getPassword().equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        String jwt = jwtUtil.generateToken(userDTO.getUsername());
        return Result.success(jwt);
    }

    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;

        String json = readCache(key);
        if (StringUtils.hasText(json)) {
            try {
                UserDetailVO cacheVO = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(cacheVO);
            } catch (Exception e) {
                log.warn("Failed to parse cache, deleting stale entry. key={}", key, e);
                deleteCache(key);
            }
        }

        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        writeCache(key, JSONUtil.toJsonStr(detail));
        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<String> updateUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.getUserId() == null) {
            return Result.error(ResultCode.ERROR);
        }

        User user = userMapper.selectById(userInfo.getUserId());
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        UserInfo existInfo = userInfoMapper.selectOne(
                new QueryWrapper<UserInfo>().eq("user_id", userInfo.getUserId()));

        if (existInfo == null) {
            userInfoMapper.insert(userInfo);
        } else {
            userInfo.setId(existInfo.getId());
            userInfoMapper.update(
                    userInfo,
                    new LambdaUpdateWrapper<UserInfo>()
                            .eq(UserInfo::getUserId, userInfo.getUserId()));
        }

        deleteCache(CACHE_KEY_PREFIX + userInfo.getUserId());
        return Result.success("\u66f4\u65b0\u6210\u529f");
    }

    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        userInfoMapper.delete(new QueryWrapper<UserInfo>().eq("user_id", userId));
        userMapper.deleteById(userId);
        deleteCache(CACHE_KEY_PREFIX + userId);
        return Result.success("\u5220\u9664\u6210\u529f");
    }

    private String readCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            log.warn("Failed to read Redis, falling back to DB. key={}", key, e);
            return null;
        }
    }

    private void writeCache(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (DataAccessException e) {
            log.warn("Failed to write Redis cache. key={}", key, e);
        }
    }

    private void deleteCache(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException e) {
            log.warn("Failed to delete Redis cache. key={}", key, e);
        }
    }
}
