package com.jaswal.portfoliotracker.repositories;

import com.jaswal.portfoliotracker.entities.Position;
import com.jaswal.portfoliotracker.enums.AssetType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByPortfolio_PortfolioIdAndSymbol(Long portfolioId, String symbol);

    List<Position> findByPortfolio_PortfolioId(Long portfolioId);

    List<Position> findByPortfolio_PortfolioId(Long portfolioId, Sort sort);

    List<Position> findByPortfolio_PortfolioIdAndAssetType(Long portfolioId, AssetType assetType);

}
