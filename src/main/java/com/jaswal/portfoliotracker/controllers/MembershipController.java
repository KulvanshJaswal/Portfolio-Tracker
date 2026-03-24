package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.Membership;
import com.jaswal.portfoliotracker.enums.Role;
import com.jaswal.portfoliotracker.services.MembershipService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

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


    @Data
    @AllArgsConstructor
    @NoArgsConstructor

    public static class addMemberRequest {
        private Long userId;
        private Role role;
    }
    @PostMapping("/api/portfolios/{portfolioId}/members")
    public Membership addMember(@PathVariable Long portfolioId, @RequestBody addMemberRequest request){
        return membershipService.addMember(portfolioId,request.getUserId(),request.getRole());
    }




}
