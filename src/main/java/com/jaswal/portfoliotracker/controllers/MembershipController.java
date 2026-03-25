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
    @NoArgsConstructor

    public static class addMemberRequest {
        private Long userId;
        private Role role;
    }
    @PostMapping("/api/portfolios/{portfolioId}/members")
    public Membership addMember(@PathVariable Long portfolioId, @RequestBody addMemberRequest request){
        return membershipService.addMember(portfolioId,request.getUserId(),request.getRole());
    }
    @DeleteMapping("/api/portfolios/{portfolioId}/members/{userId}")
    public void removeMember(@PathVariable Long portfolioId, @PathVariable Long userId){
        membershipService.removeMember(portfolioId, userId);

    }

    @Data
    @NoArgsConstructor
    public static class updateMemberRequest{
        private Role role;
    }

    @PutMapping("/api/portfolios/{portfolioId}/members/{userId}")
    public Membership updateMembership(@PathVariable Long portfolioId,
                                       @PathVariable Long userId,
                                       @RequestBody updateMemberRequest request ){
        return membershipService.updateMemberRole(portfolioId,userId,request.getRole());

    }

    @GetMapping("/api/portfolios/{portfolioId}")
    public List<Membership> getPortfolioMembers(@PathVariable Long portfolioId){
        return membershipService.getPortfolioMembers(portfolioId);
    }
    @GetMapping("/api/portfolios/{userId}")
    public List<Membership> getUsersMemberships(@PathVariable Long userId){
        return membershipService.getUserMemberships(userId);
    }

    GetMapping






}
