package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.Invite;
import com.jaswal.portfoliotracker.entities.Membership;
import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.enums.Role;
import com.jaswal.portfoliotracker.repositories.InviteRepository;
import com.jaswal.portfoliotracker.repositories.MembershipRepository;
import com.jaswal.portfoliotracker.repositories.PortfolioRepository;
import com.jaswal.portfoliotracker.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest(classes = PortfolioTrackerApplication.class)
@Transactional
public class MembershipServiceTest {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private InviteRepository inviteRepository;

    private User testUser;
    private User otherUser;
    private Portfolio testPortfolio;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser = userRepository.save(testUser);

        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@test.com");
        otherUser = userRepository.save(otherUser);

        testPortfolio = new Portfolio();
        testPortfolio.setCreatedBy(testUser);
        testPortfolio.setName("Test Portfolio");
        testPortfolio = portfolioRepository.save(testPortfolio);
    }


    @Test
    void testAddMember_Success() {
        Membership membership = membershipService.addMember(
                testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN);

        assertNotNull(membership.getMembershipId());
        assertEquals("User match", testUser.getUserId(), membership.getUser().getUserId());
        assertEquals("Portfolio match", testPortfolio.getPortfolioId(), membership.getPortfolio().getPortfolioId());
        assertEquals("Role match", Role.ADMIN, membership.getRole());
    }

    @Test
    void testAddMember_NullPortfolioId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.addMember(null, testUser.getUserId(), Role.ADMIN)
        );
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void testAddMember_NullUserId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.addMember(testPortfolio.getPortfolioId(), null, Role.ADMIN)
        );
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void testAddMember_NullRole_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), null)
        );
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void testAddMember_NonExistentPortfolio_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.addMember(99999L, testUser.getUserId(), Role.ADMIN)
        );
        assertTrue(exception.getMessage().contains("Portfolio not found"));
    }

    @Test
    void testAddMember_NonExistentUser_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.addMember(testPortfolio.getPortfolioId(), 99999L, Role.ADMIN)
        );
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testAddMember_DuplicateMembership_ThrowsException() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.MEMBER)
        );
        assertTrue(exception.getMessage().contains("already a member"));
    }


    @Test
    void testRemoveMember_Success() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN);

        membershipService.removeMember(testPortfolio.getPortfolioId(), testUser.getUserId());

        assertFalse(membershipRepository.findByUser_UserIdAndPortfolio_PortfolioId(
                testUser.getUserId(), testPortfolio.getPortfolioId()).isPresent());
    }

    @Test
    void testRemoveMember_NonExistentMembership_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.removeMember(testPortfolio.getPortfolioId(), testUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("Membership not found"));
    }


    @Test
    void testUpdateMemberRole_Success() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.MEMBER);

        Membership updated = membershipService.updateMemberRole(
                testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN);

        assertEquals("Role updated", Role.ADMIN, updated.getRole());
    }

    @Test
    void testUpdateMemberRole_NullRole_ThrowsException() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.MEMBER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.updateMemberRole(testPortfolio.getPortfolioId(), testUser.getUserId(), null)
        );
        assertTrue(exception.getMessage().contains("Role cannot be null"));
    }

    @Test
    void testUpdateMemberRole_NonExistentMembership_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.updateMemberRole(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN)
        );
        assertTrue(exception.getMessage().contains("Membership not found"));
    }


    @Test
    void testGetPortfolioMembers_ReturnsMembers() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN);
        membershipService.addMember(testPortfolio.getPortfolioId(), otherUser.getUserId(), Role.MEMBER);

        List<Membership> members = membershipService.getPortfolioMembers(testPortfolio.getPortfolioId());

        assertEquals("Member count", 2, members.size());
    }

    @Test
    void testGetPortfolioMembers_EmptyList() {
        List<Membership> members = membershipService.getPortfolioMembers(testPortfolio.getPortfolioId());

        assertTrue(members.isEmpty());
    }

    @Test
    void testGetPortfolioMembers_NonExistentPortfolio_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.getPortfolioMembers(99999L)
        );
        assertTrue(exception.getMessage().contains("Portfolio not found"));
    }


    @Test
    void testGetUserMemberships_ReturnsMemberships() {
        Portfolio secondPortfolio = new Portfolio();
        secondPortfolio.setCreatedBy(testUser);
        secondPortfolio.setName("Second Portfolio");
        secondPortfolio = portfolioRepository.save(secondPortfolio);

        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN);
        membershipService.addMember(secondPortfolio.getPortfolioId(), testUser.getUserId(), Role.MEMBER);

        List<Membership> memberships = membershipService.getUserMemberships(testUser.getUserId());

        assertEquals("Membership count", 2, memberships.size());
    }

    @Test
    void testGetUserMemberships_EmptyList() {
        List<Membership> memberships = membershipService.getUserMemberships(testUser.getUserId());

        assertTrue(memberships.isEmpty());
    }

    @Test
    void testGetUserMemberships_NonExistentUser_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.getUserMemberships(99999L)
        );
        assertTrue(exception.getMessage().contains("User not found"));
    }


    @Test
    void testHasAccess_True() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.MEMBER);

        assertTrue(membershipService.hasAccess(testUser.getUserId(), testPortfolio.getPortfolioId()));
    }

    @Test
    void testHasAccess_False() {
        assertFalse(membershipService.hasAccess(testUser.getUserId(), testPortfolio.getPortfolioId()));
    }


    @Test
    void testHasRole_AdminHasAllPermissions() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.ADMIN);

        assertTrue(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.ADMIN));
        assertTrue(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.MEMBER));
        assertTrue(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.VISITOR));
    }

    @Test
    void testHasRole_MemberCannotBeAdmin() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.MEMBER);

        assertFalse(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.ADMIN));
        assertTrue(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.MEMBER));
        assertTrue(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.VISITOR));
    }

    @Test
    void testHasRole_VisitorOnlyVisitor() {
        membershipService.addMember(testPortfolio.getPortfolioId(), testUser.getUserId(), Role.VISITOR);

        assertFalse(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.ADMIN));
        assertFalse(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.MEMBER));
        assertTrue(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.VISITOR));
    }

    @Test
    void testHasRole_NoMembership_ReturnsFalse() {
        assertFalse(membershipService.hasRole(testUser.getUserId(), testPortfolio.getPortfolioId(), Role.VISITOR));
    }


    @Test
    void testCreateInvite_Success() {
        Invite invite = membershipService.createInvite(
                testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        assertNotNull(invite.getInviteId());
        assertNotNull(invite.getInviteCode());
        assertEquals("Invite code length", 8, invite.getInviteCode().length());
        assertEquals("Role match", Role.MEMBER, invite.getRole());
        assertEquals("Max uses", 5, invite.getMaxUses());
        assertEquals("Current uses starts at 0", 0, invite.getCurrentUses());
        assertNotNull(invite.getCreatedAt());
        assertNotNull(invite.getExpiresAt());
        assertTrue(invite.getExpiresAt().isAfter(invite.getCreatedAt()));
    }

    @Test
    void testCreateInvite_NonExistentUser_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.createInvite(testPortfolio.getPortfolioId(), 99999L, 7, 5, Role.MEMBER)
        );
        assertTrue(exception.getMessage().contains("Must be a user"));
    }

    @Test
    void testCreateInvite_NonExistentPortfolio_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.createInvite(99999L, testUser.getUserId(), 7, 5, Role.MEMBER)
        );
        assertTrue(exception.getMessage().contains("Must have a valid portfolio"));
    }


    @Test
    void testRedeemInvite_Success() {
        Invite invite = membershipService.createInvite(
                testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        Membership membership = membershipService.redeemInvite(invite.getInviteCode(), otherUser.getUserId());

        assertNotNull(membership.getMembershipId());
        assertEquals("User match", otherUser.getUserId(), membership.getUser().getUserId());
        assertEquals("Portfolio match", testPortfolio.getPortfolioId(), membership.getPortfolio().getPortfolioId());
        assertEquals("Role from invite", Role.MEMBER, membership.getRole());

        Invite updatedInvite = inviteRepository.findById(invite.getInviteId()).orElseThrow();
        assertEquals("Uses incremented", 1, updatedInvite.getCurrentUses());
    }

    @Test
    void testRedeemInvite_NullCode_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.redeemInvite(null, otherUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void testRedeemInvite_EmptyCode_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.redeemInvite("", otherUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void testRedeemInvite_NullUserId_ThrowsException() {
        Invite invite = membershipService.createInvite(
                testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.redeemInvite(invite.getInviteCode(), null)
        );
        assertTrue(exception.getMessage().contains("User ID cannot be null"));
    }

    @Test
    void testRedeemInvite_InvalidCode_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.redeemInvite("INVALID1", otherUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("Invalid invite code"));
    }

    @Test
    void testRedeemInvite_ExpiredInvite_ThrowsException() {
        Invite invite = new Invite();
        invite.setPortfolio(testPortfolio);
        invite.setCreatedBy(testUser);
        invite.setInviteCode("EXPIRED1");
        invite.setRole(Role.MEMBER);
        invite.setMaxUses(5);
        invite.setCurrentUses(0);
        invite.setCreatedAt(LocalDateTime.now().minusDays(10));
        invite.setExpiresAt(LocalDateTime.now().minusDays(1)); // Already expired
        inviteRepository.save(invite);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.redeemInvite("EXPIRED1", otherUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void testRedeemInvite_MaxUsesReached_ThrowsException() {
        Invite invite = new Invite();
        invite.setPortfolio(testPortfolio);
        invite.setCreatedBy(testUser);
        invite.setInviteCode("MAXUSED1");
        invite.setRole(Role.MEMBER);
        invite.setMaxUses(1);
        invite.setCurrentUses(1); // Already used up
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        inviteRepository.save(invite);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.redeemInvite("MAXUSED1", otherUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("maximum uses"));
    }

    @Test
    void testRedeemInvite_UserAlreadyMember_ThrowsException() {
        membershipService.addMember(testPortfolio.getPortfolioId(), otherUser.getUserId(), Role.VISITOR);

        Invite invite = membershipService.createInvite(
                testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipService.redeemInvite(invite.getInviteCode(), otherUser.getUserId())
        );
        assertTrue(exception.getMessage().contains("already a member"));
    }

    @Test
    void testRedeemInvite_NonExistentUser_ThrowsException() {
        Invite invite = membershipService.createInvite(
                testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.redeemInvite(invite.getInviteCode(), 99999L)
        );
        assertTrue(exception.getMessage().contains("User not found"));
    }


    @Test
    void testGetPortfolioInvites_ReturnsInvites() {
        membershipService.createInvite(testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);
        membershipService.createInvite(testPortfolio.getPortfolioId(), testUser.getUserId(), 3, 10, Role.VISITOR);

        List<Invite> invites = membershipService.getPortfolioInvites(testPortfolio.getPortfolioId());

        assertEquals("Invite count", 2, invites.size());
    }

    @Test
    void testGetPortfolioInvites_EmptyList() {
        List<Invite> invites = membershipService.getPortfolioInvites(testPortfolio.getPortfolioId());

        assertTrue(invites.isEmpty());
    }

    @Test
    void testGetPortfolioInvites_NonExistentPortfolio_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.getPortfolioInvites(99999L)
        );
        assertTrue(exception.getMessage().contains("Portfolio not found"));
    }


    @Test
    void testGetActiveInvites_OnlyReturnsNonExpired() {
        membershipService.createInvite(testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        Invite expired = new Invite();
        expired.setPortfolio(testPortfolio);
        expired.setCreatedBy(testUser);
        expired.setInviteCode("OLDCODE1");
        expired.setRole(Role.VISITOR);
        expired.setMaxUses(5);
        expired.setCurrentUses(0);
        expired.setCreatedAt(LocalDateTime.now().minusDays(10));
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));
        inviteRepository.save(expired);

        List<Invite> activeInvites = membershipService.getActiveInvites(testPortfolio.getPortfolioId());

        assertEquals("Only active invite returned", 1, activeInvites.size());
    }

    @Test
    void testGetActiveInvites_NonExistentPortfolio_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.getActiveInvites(99999L)
        );
        assertTrue(exception.getMessage().contains("Portfolio not found"));
    }


    @Test
    void testDeleteInvite_Success() {
        Invite invite = membershipService.createInvite(
                testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        membershipService.deleteInvite(invite.getInviteId());

        assertFalse(inviteRepository.findById(invite.getInviteId()).isPresent());
    }

    @Test
    void testDeleteInvite_NonExistentInvite_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.deleteInvite(99999L)
        );
        assertTrue(exception.getMessage().contains("Invite not found"));
    }


    @Test
    void testGetInviteByCode_Success() {
        Invite created = membershipService.createInvite(
                testPortfolio.getPortfolioId(), testUser.getUserId(), 7, 5, Role.MEMBER);

        Invite found = membershipService.getInviteByCode(created.getInviteCode());

        assertEquals("Invite ID match", created.getInviteId(), found.getInviteId());
        assertEquals("Invite code match", created.getInviteCode(), found.getInviteCode());
    }

    @Test
    void testGetInviteByCode_InvalidCode_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                membershipService.getInviteByCode("DOESNTEX")
        );
        assertTrue(exception.getMessage().contains("Invite not found"));
    }
}

