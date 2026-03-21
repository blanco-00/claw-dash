package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.config.JwtTokenProvider;
import com.clawdash.entity.User;
import com.clawdash.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");

        User user = userService.register(username, password, email);
        return Result.success("Registration successful", user);
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        if (user == null) {
            return Result.error(401, "Invalid username or password");
        }

        if (!userService.verifyPassword(password, user.getPassword())) {
            return Result.error(401, "Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(username);
        return Result.success(Map.of("token", token, "username", username));
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String authHeader) {
        String username = jwtTokenProvider.getUsernameFromToken(authHeader.replace("Bearer ", ""));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        if (user == null) {
            return Result.error(404, "User not found");
        }

        if (!userService.verifyPassword(oldPassword, user.getPassword())) {
            return Result.error(400, "Old password is incorrect");
        }

        user.setPassword(userService.encodePassword(newPassword));
        userService.updateById(user);
        
        return Result.success("Password changed successfully", null);
    }
}
