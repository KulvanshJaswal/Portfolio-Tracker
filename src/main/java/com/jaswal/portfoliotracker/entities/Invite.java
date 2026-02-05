package com.jaswal.portfoliotracker.entities;

import com.jaswal.portfoliotracker.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invites")
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long inviteId;
    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
    @Column(name = "invite_code", nullable = false, unique = true)
    private String inviteCode;
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;
    @Column(name = "current_uses", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer currentUses;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
