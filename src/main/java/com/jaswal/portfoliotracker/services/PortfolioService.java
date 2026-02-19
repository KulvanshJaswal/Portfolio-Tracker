package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.Position;
import com.jaswal.portfoliotracker.entities.PriceQuote;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.repositories.PortfolioRepository;
import com.jaswal.portfoliotracker.repositories.PositionRepository;
import com.jaswal.portfoliotracker.repositories.PriceQuoteRepository;
import org.springframework.stereotype.Service;

import javax.sound.sampled.Port;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final PriceQuoteService priceQuoteService;
    private final PositionService positionService;

    public PortfolioService(PortfolioRepository portfolioRepository, PositionRepository positionRepository, PriceQuoteService priceQuoteService, PositionService positionService){
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.positionService = positionService;
        this.priceQuoteService = priceQuoteService;

        }
    public Portfolio createPortfolio(User user, String portfolioName){
        Portfolio portfolio = new Portfolio();

        portfolio.setCreatedBy(user);

        portfolio.setName(portfolioName);

        return portfolioRepository.save(portfolio);
    }
    public Portfolio getPortfolio(Long id){
        return portfolioRepository.findById(id).orElseThrow(() -> new RuntimeException("Portfolio not found with id" + id));
    }

    public Portfolio changePortfolioName(Long id, String portfolioName){
        if(portfolioName ==null || portfolioName.trim().isEmpty()){
            throw new IllegalArgumentException("The name of the Portfolio can't be empty");
        }

        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Portfolio not found with id" + id));

        portfolio.setName(portfolioName);

        return portfolioRepository.save(portfolio);
    }
    public void deletePortfolio(Long id){
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Portfolio not found with id" + id));
        portfolioRepository.delete(portfolio);
    }

    public BigDecimal calculateProtfolioPnl(Long portfolio_id){
        BigDecimal sum = BigDecimal.ZERO;
        List<Position> positions = positionService.getPositionsForPortfolio(portfolio_id);
        for(Position position: positions){
            BigDecimal current_price = priceQuoteService.getCurrentPrice(position.getSymbol());
            sum = sum.add(positionService.getPositionPnl(position, current_price));
        }
        return sum;
    }
    public BigDecimal calculatePortfolioValue(Long portfolio_id) {
        BigDecimal sum = BigDecimal.ZERO;
        List<Position> positions = positionService.getPositionsForPortfolio(portfolio_id);
        for (Position position : positions) {
            BigDecimal current_price = priceQuoteService.getCurrentPrice(position.getSymbol());
            BigDecimal quantity = position.getTotalQuantity();
            sum = sum.add(quantity.multiply(current_price));

        }
        return sum;
        }
    public List<Portfolio> findUsersPortfolios(Long user_id) {
        List<Portfolio> portfolios = portfolioRepository.findByCreatedBy_UserId(user_id);
        return portfolios;
    }


}


