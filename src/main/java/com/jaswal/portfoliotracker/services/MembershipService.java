package com.jaswal.portfoliotracker.services;


import com.jaswal.portfoliotracker.entities.Membership;
import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.enums.Role;
import com.jaswal.portfoliotracker.repositories.InviteRepository;
import com.jaswal.portfoliotracker.repositories.MembershipRepository;
import com.jaswal.portfoliotracker.repositories.PortfolioRepository;
import com.jaswal.portfoliotracker.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Membership addMember(Long portfolioId, Long userId, Role role) {
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
        Membership membership = membershipRepository
                .findByUser_UserIdAndPortfolio_PortfolioId(userId, portfolioId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        membershipRepository.delete(membership);
    }
    public Membership updateMemberRole(Long portfolioId, Long userId, Role newRole) {
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

    private boolean hasPermission(Role userRole, Role requiredRole) {
        if (userRole == Role.ADMIN) return true;
        if (userRole == Role.MEMBER) return requiredRole != Role.ADMIN;
        return requiredRole == Role.VISITOR;
    }

}
