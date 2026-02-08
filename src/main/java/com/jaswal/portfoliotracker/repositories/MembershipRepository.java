package com.jaswal.portfoliotracker.repositories;

import com.jaswal.portfoliotracker.entities.Membership;
import com.jaswal.portfoliotracker.entities.Portfolio;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUser_UserId(Long userUserId);

    List<Membership> findByPortfolio_PortfolioId(Long portfolioId);

    Optional<Membership> findByUser_UserIdAndPortfolio_PortfolioId(Long userId, Long portfolioId);

}
