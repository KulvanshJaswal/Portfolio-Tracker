package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.Membership;
import com.jaswal.portfoliotracker.services.MembershipService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping("/api/portfolios/{portfolioId}/members")
    public List<Membership> getMembers(@PathVariable Long portfolioId){
        return membershipService.getPortfolioMembers(portfolioId);


    }

}
