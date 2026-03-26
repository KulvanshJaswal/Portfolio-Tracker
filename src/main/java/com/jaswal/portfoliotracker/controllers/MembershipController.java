package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.Invite;
import com.jaswal.portfoliotracker.entities.Membership;
import com.jaswal.portfoliotracker.enums.Role;
import com.jaswal.portfoliotracker.services.MembershipService;
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
    private static class AddMemberRequest {
        private Long userId;
        private Role role;
    }

    @PostMapping("/api/portfolios/{portfolioId}/members")
    public Membership addMember(@PathVariable Long portfolioId, @RequestBody AddMemberRequest request){
        return membershipService.addMember(portfolioId, request.getUserId(), request.getRole());
    }

    @DeleteMapping("/api/portfolios/{portfolioId}/members/{userId}")
    public void removeMember(@PathVariable Long portfolioId, @PathVariable Long userId){
        membershipService.removeMember(portfolioId, userId);
    }

    @Data
    @NoArgsConstructor
    private static class UpdateMemberRequest {
        private Role role;
    }

    @PutMapping("/api/portfolios/{portfolioId}/members/{userId}")
    public Membership updateMembership(@PathVariable Long portfolioId,
                                       @PathVariable Long userId,
                                       @RequestBody UpdateMemberRequest request){
        return membershipService.updateMemberRole(portfolioId, userId, request.getRole());
    }

    @GetMapping("/api/users/{userId}/memberships")
    public List<Membership> getUsersMemberships(@PathVariable Long userId){
        return membershipService.getUserMemberships(userId);
    }

    @Data
    @NoArgsConstructor
    private static class CreateInviteRequest {
        private Long userId;
        private Integer daysForExpiry;
        private Integer maxUses;
        private Role userRole;
    }

    @PostMapping("/api/portfolios/{portfolioId}/invites")
    public Invite createInvite(@PathVariable Long portfolioId, @RequestBody CreateInviteRequest request){
        return membershipService.createInvite(portfolioId,
                request.getUserId(),
                request.getDaysForExpiry(),
                request.getMaxUses(),
                request.getUserRole());
    }

    @PostMapping("/api/invites/{inviteCode}/redeem/{userId}")
    public Membership redeemMembership(@PathVariable String inviteCode, @PathVariable Long userId){
        return membershipService.redeemInvite(inviteCode, userId);
    }

    @GetMapping("/api/portfolios/{portfolioId}/invites")
    public List<Invite> getPortfolioInvites(@PathVariable Long portfolioId){
        return membershipService.getPortfolioInvites(portfolioId);
    }

    @GetMapping("/api/portfolios/{portfolioId}/invites/active")
    public List<Invite> getActiveInvites(@PathVariable Long portfolioId){
        return membershipService.getActiveInvites(portfolioId);
    }

    @DeleteMapping("/api/invites/{inviteId}")
    public void deleteInvite(@PathVariable Long inviteId){
        membershipService.deleteInvite(inviteId);
    }

    @GetMapping("/api/invites/{inviteCode}")
    public Invite getInviteByCode(@PathVariable String inviteCode){
        return membershipService.getInviteByCode(inviteCode);
    }
}