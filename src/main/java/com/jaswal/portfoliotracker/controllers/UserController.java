package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("")
    public User createUser(@RequestBody Map<String, String> request) {
        return userService.createUsername(
                request.get("username"),
                request.get("email")
        );
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