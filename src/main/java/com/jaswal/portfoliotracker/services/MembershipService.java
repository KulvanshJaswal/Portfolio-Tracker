package com.jaswal.portfoliotracker.services;


import com.jaswal.portfoliotracker.config.AuthUtils;
import com.jaswal.portfoliotracker.entities.Invite;
import com.jaswal.portfoliotracker.entities.Membership;
import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.enums.Role;
import com.jaswal.portfoliotracker.repositories.InviteRepository;
import com.jaswal.portfoliotracker.repositories.MembershipRepository;
import com.jaswal.portfoliotracker.repositories.PortfolioRepository;
import com.jaswal.portfoliotracker.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;



    public MembershipService(MembershipRepository membershipRepository, InviteRepository inviteRepository, UserRepository userRepository, PortfolioRepository portfolioRepository) {
        this.membershipRepository = membershipRepository;
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public Membership createOwnerMembership(Portfolio portfolio, User user) {
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setPortfolio(portfolio);
        membership.setRole(Role.ADMIN);
        return membershipRepository.save(membership);
    }

    public Membership addMember(Long portfolioId, Long userId, Role role) {
        requireAdmin(portfolioId);
        // Validate inputs
        if (portfolioId == null || userId == null || role == null) {
            throw new IllegalArgumentException("Portfolio ID, User ID, and Role cannot be null");
        }

        // Check if portfolio exists
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if membership already exists
        if (membershipRepository.findByUser_UserIdAndPortfolio_PortfolioId(userId, portfolioId).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this portfolio");
        }

        // Create and save membership
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setPortfolio(portfolio);
        membership.setRole(role);

        return membershipRepository.save(membership);
    }
    public void removeMember(Long portfolioId, Long userId) {
        requireAdmin(portfolioId);
        Membership membership = membershipRepository
                .findByUser_UserIdAndPortfolio_PortfolioId(userId, portfolioId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        membershipRepository.delete(membership);
    }
    public Membership updateMemberRole(Long portfolioId, Long userId, Role newRole) {
        requireAdmin(portfolioId);
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        Membership membership = membershipRepository
                .findByUser_UserIdAndPortfolio_PortfolioId(userId, portfolioId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        membership.setRole(newRole);
        return membershipRepository.save(membership);
    }
    public List<Membership> getPortfolioMembers(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new RuntimeException("Portfolio not found with id: " + portfolioId);
        }
        return membershipRepository.findByPortfolio_PortfolioId(portfolioId);
    }

    public List<Membership> getUserMemberships(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return membershipRepository.findByUser_UserId(userId);
    }
    public boolean hasAccess(Long userId, Long portfolioId) {
        return membershipRepository
                .findByUser_UserIdAndPortfolio_PortfolioId(userId, portfolioId)
                .isPresent();
    }

    public boolean hasRole(Long userId, Long portfolioId, Role requiredRole) {
        return membershipRepository
                .findByUser_UserIdAndPortfolio_PortfolioId(userId, portfolioId)
                .map(membership -> hasPermission(membership.getRole(), requiredRole))
                .orElse(false);
    }

    public void requireAdmin(Long portfolioId) {
        Long userId = AuthUtils.getCurrentUserId();
        if (!hasRole(userId, portfolioId, Role.ADMIN)) {
            throw new RuntimeException("Access denied: Admin role required");
        }
    }

    public void requireMemberOrAbove(Long portfolioId) {
        Long userId = AuthUtils.getCurrentUserId();
        if (!hasRole(userId, portfolioId, Role.MEMBER)) {
            throw new RuntimeException("Access denied: Member role or above required");
        }
    }

    public void requireVisitorOrAbove(Long portfolioId) {
        Long userId = AuthUtils.getCurrentUserId();
        if (!hasRole(userId, portfolioId, Role.VISITOR)) {
            throw new RuntimeException("Access denied: Portfolio membership required");
        }
    }

    private boolean hasPermission(Role userRole, Role requiredRole) {
        if (userRole == Role.ADMIN) return true;
        if (userRole == Role.MEMBER) return requiredRole != Role.ADMIN;
        return requiredRole == Role.VISITOR;
    }
    private String generateInvitePassword(){
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }while(inviteRepository.findByInviteCode(code).isPresent());
        return code;
    }
    public Invite createInvite(Long portfolioId, Long userId, Integer daysForExpiry, Integer maxUses,Role userRole){
        requireAdmin(portfolioId);
        Invite invite = new Invite();
        invite.setCreatedBy(userRepository.findById(userId).orElseThrow(()
                ->new RuntimeException("Must be a user of the Portfolio to send an invite")));
        invite.setPortfolio(portfolioRepository.findById(portfolioId).orElseThrow(()
                ->new RuntimeException("Must have a valid portfolio to invite a user to.")));
        invite.setInviteCode(generateInvitePassword());
        invite.setRole(userRole);
        invite.setMaxUses(maxUses);
        LocalDateTime timeNow = LocalDateTime.now();
        invite.setCreatedAt(timeNow);
        invite.setExpiresAt(timeNow.plusDays(daysForExpiry));
        invite.setCurrentUses(0);
        return inviteRepository.save(invite);

    }

    public Membership redeemInvite(String inviteCode, Long userId) {
        // Validate inputs
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Invite code cannot be empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Find invite
        Invite invite = inviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        // Check if invite is still valid
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite code has expired");
        }

        // Check if invite has uses remaining
        if (invite.getCurrentUses() >= invite.getMaxUses()) {
            throw new IllegalArgumentException("Invite code has reached maximum uses");
        }

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if user is already a member
        if (membershipRepository.findByUser_UserIdAndPortfolio_PortfolioId(
                userId, invite.getPortfolio().getPortfolioId()).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this portfolio");
        }

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setPortfolio(invite.getPortfolio());
        membership.setRole(invite.getRole());

        invite.setCurrentUses(invite.getCurrentUses() + 1);
        inviteRepository.save(invite);

        return membershipRepository.save(membership);
    }

    public List<Invite> getPortfolioInvites(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new RuntimeException("Portfolio not found with id: " + portfolioId);
        }
        return inviteRepository.findByPortfolio_PortfolioId(portfolioId);
    }


    public List<Invite> getActiveInvites(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new RuntimeException("Portfolio not found with id: " + portfolioId);
        }
        return inviteRepository.findByPortfolio_PortfolioIdAndExpiresAtAfter(
                portfolioId, LocalDateTime.now());
    }

    public void deleteInvite(Long inviteId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found with id: " + inviteId));
        requireAdmin(invite.getPortfolio().getPortfolioId());
        inviteRepository.delete(invite);
    }

    public Invite getInviteByCode(String inviteCode) {
        return inviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invite not found with code: " + inviteCode));
    }

}
