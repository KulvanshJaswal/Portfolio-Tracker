package com.jaswal.portfoliotracker.config;

import com.jaswal.portfoliotracker.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
    public static Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return user.getUserId();
    }
}