package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.UserInfo;
import com.example.demo.vo.UserDetailVO;

public interface UserService {

    Result<String> register(UserDTO userDTO);

    Result<String> login(UserDTO userDTO);

    Result<UserDetailVO> getUserDetail(Long userId);

    Result<String> updateUserInfo(UserInfo userInfo);

    Result<String> deleteUser(Long userId);
}
