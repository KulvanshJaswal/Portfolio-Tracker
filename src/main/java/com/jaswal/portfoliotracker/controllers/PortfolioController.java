package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.repositories.UserRepository;
import com.jaswal.portfoliotracker.services.PortfolioService;
import com.jaswal.portfoliotracker.services.UserService;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.Port;
import java.util.List;
import java.util.Map;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    public PortfolioController(PortfolioService portfolioService,  UserService userService) {
        this.portfolioService = portfolioService;
        this.userService =  userService;
    }

    @PostMapping("/api/users/{userId}/portfolios")
    public Portfolio createPortfolio(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request
            ) {
        User user = userService.getUser(userId);
        return portfolioService.createPortfolio(user, request.get("name"));
    }

    @GetMapping("/api/users/{userId}/portfolios")
    public List<Portfolio> getAllPortfoliosForUsers(@PathVariable Long userId){
        return portfolioService.findUsersPortfolios(userId);
    }
    @GetMapping("/api/portfolios/{portfolioId}")
    public Portfolio getPortfolio(@PathVariable Long portfolioId){
        return portfolioService.getPortfolio(portfolioId);
    }
}