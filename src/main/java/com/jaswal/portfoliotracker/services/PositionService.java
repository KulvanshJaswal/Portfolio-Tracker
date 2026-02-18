package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.Position;
import com.jaswal.portfoliotracker.repositories.PortfolioRepository;
import com.jaswal.portfoliotracker.repositories.PositionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository;

    public PositionService(PositionRepository positionRepository, PortfolioRepository portfolioRepository) {
        this.positionRepository = positionRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public List<Position> getPositionsForPortfolio(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new IllegalArgumentException("Portfolio does not exist");
        }
        return positionRepository.findByPortfolio_PortfolioId(portfolioId);
    }

    public Position getPositionBySymbol(Long portfolioId, String symbol) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new IllegalArgumentException("Portfolio does not exist");
        }
        return positionRepository.findByPortfolio_PortfolioIdAndSymbol(portfolioId, symbol)
                .orElseThrow(() -> new IllegalArgumentException("Position not found for symbol: " + symbol));
    }

    public BigDecimal getPositionPnl(Position position, BigDecimal currentPrice) {
        //Initial Checking
        if (position == null) {
            throw new IllegalArgumentException("Position does not exist");
        }
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current price is invalid");
        }
        //Computing PNL
        return position.getTotalQuantity().multiply(currentPrice).subtract(position.getTotalCost());
    }
}