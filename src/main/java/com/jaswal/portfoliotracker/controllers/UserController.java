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
}