package com.jaswal.portfoliotracker.repositories;

import com.jaswal.portfoliotracker.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByCreatedBy_UserIdAndName(Long userId, String name);

}
