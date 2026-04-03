package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.components.JwtUtil;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("")
    public User createUser(@RequestBody Map<String, String> request) {
        return userService.createUsername(
                request.get("username"),
                request.get("email"),
                request.get("password")
        );
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        User user = userService.checkPassword(username, email, password);
        return jwtUtil.createToken(user.getUserId());
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PutMapping("/{userId}/email")
    public User updateUserEmail(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        return userService.updateEmail(userId, request.get("email"));
    }

    @PutMapping("/{userId}/username")
    public User updateUserUsername(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        return userService.updateUser(userId, request.get("username"));
    }
}