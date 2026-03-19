package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 登录接口（放行）
    @PostMapping("/login")
    public Result<String> login() {
        String token = "mock-token-123456";
        return Result.success("登录成功，你的 token 是：" + token);
    }

    // 查询用户（放行：GET /api/users/{id}）
    @GetMapping("/{id}")
    public Result<String> getUser(@PathVariable("id") Long id) {
        String data = "查询成功，正在返回 ID 为 " + id + " 的用户信息";
        return Result.success(data);
    }

    // 新增用户（放行：POST /api/users）
    @PostMapping
    public Result<String> createUser(@RequestBody User user) {
        String data = "新增成功，接收到用户：" + user.getName() + "，年龄：" + user.getAge();
        return Result.success(data);
    }

    // 修改用户（需要 token）
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        String data = "更新成功，ID " + id + " 的用户已修改为：" + user.getName();
        return Result.success(data);
    }

    // 删除用户（需要 token）
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        String data = "删除成功，已删除 ID 为 " + id + " 的用户";
        return Result.success(data);
    }
}