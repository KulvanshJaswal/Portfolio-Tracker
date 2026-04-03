package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.services.PortfolioService;
import com.jaswal.portfoliotracker.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    public PortfolioController(PortfolioService portfolioService, UserService userService) {
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

    @GetMapping("/api/portfolios")
    public List<Portfolio> getAllPortfoliosForUsers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return portfolioService.findUsersPortfolios(userId);
    }

    @GetMapping("/api/portfolios/{portfolioId}")
    public Portfolio getPortfolio(@PathVariable Long portfolioId){
        return portfolioService.getPortfolio(portfolioId);
    }

    @PutMapping("/api/portfolios/{portfolioId}")
    public Portfolio updatePortfolio(@PathVariable Long portfolioId, @RequestBody Map<String, String> request){
        return portfolioService.changePortfolioName(portfolioId, request.get("name"));
    }

    @DeleteMapping("/api/portfolios/{portfolioId}")
    public void deletePortfolio(@PathVariable Long portfolioId){
        portfolioService.deletePortfolio(portfolioId);
    }
}