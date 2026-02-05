package com.jaswal.portfoliotracker.entities;

import com.jaswal.portfoliotracker.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "membership",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "portfolio_id"}))
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;
    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne()
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

}
