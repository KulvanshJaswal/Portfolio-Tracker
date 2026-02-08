package com.jaswal.portfoliotracker.repositories;

import com.jaswal.portfoliotracker.entities.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    Optional<Invite> findByInviteCode(String inviteCode);

    List<Invite> findByPortfolio_PortfolioId(Long portfolioId);

    List<Invite> findByPortfolio_PortfolioIdAndExpiresAtAfter(Long portfolioPortfolioId, LocalDateTime expiresAtAfter);

}
