package com.jaswal.portfoliotracker.repositories;

import com.jaswal.portfoliotracker.entities.PriceQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceQuoteRepository extends JpaRepository<PriceQuote, String> {

    List<PriceQuote> findBySource(String source);

    List<PriceQuote> findByLastUpdatedAfter(LocalDateTime timestamp);

    List<PriceQuote> findByLastUpdatedBetween(LocalDateTime start, LocalDateTime end);
}