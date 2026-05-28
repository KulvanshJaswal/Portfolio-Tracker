package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.config.AuthUtils;
import com.jaswal.portfoliotracker.dto.PortfolioSummary;
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
    private final MembershipService membershipService;

    public PortfolioService(PortfolioRepository portfolioRepository, PositionRepository positionRepository, PriceQuoteService priceQuoteService, PositionService positionService, MembershipService membershipService){
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.positionService = positionService;
        this.priceQuoteService = priceQuoteService;
        this.membershipService = membershipService;
    }
    public Portfolio createPortfolio(User user, String portfolioName){
        Portfolio portfolio = new Portfolio();
        portfolio.setCreatedBy(user);
        portfolio.setName(portfolioName);
        Portfolio saved = portfolioRepository.save(portfolio);
        membershipService.createOwnerMembership(saved, user);
        return saved;
    }
    public Portfolio getPortfolio(Long id){
        return portfolioRepository.findById(id).orElseThrow(() -> new RuntimeException("Portfolio not found with id" + id));
    }

    public Portfolio changePortfolioName(Long id, String portfolioName){
        membershipService.requireAdmin(id);
        if(portfolioName ==null || portfolioName.trim().isEmpty()){
            throw new IllegalArgumentException("The name of the Portfolio can't be empty");
        }

        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Portfolio not found with id" + id));

        portfolio.setName(portfolioName);

        return portfolioRepository.save(portfolio);
    }
    public void deletePortfolio(Long id){
        membershipService.requireAdmin(id);
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Portfolio not found with id" + id));
        portfolioRepository.delete(portfolio);
    }

    public BigDecimal calculatePortfolioPnl(Long portfolio_id){
        Long userId = AuthUtils.getCurrentUserId();
        if (!membershipService.hasAccess(userId, portfolio_id)) {
            throw new RuntimeException("Access denied: You are not a member of this portfolio");
        }
        BigDecimal sum = BigDecimal.ZERO;
        List<Position> positions = positionService.getPositionsForPortfolio(portfolio_id);
        for(Position position: positions){
            if (position.getSymbol().equals("CASH")){
                continue;
            }
            try {
                BigDecimal current_price = priceQuoteService.getCachedPrice(position.getSymbol(), position.getAssetType());
                sum = sum.add(positionService.getPositionPnl(position, current_price));
            } catch (Exception ignored) {
                // No market price cached for this position — skip it
            }
        }
        return sum;
    }

    public BigDecimal calculatePortfolioValue(Long portfolio_id) {
        Long userId = AuthUtils.getCurrentUserId();
        if (!membershipService.hasAccess(userId, portfolio_id)) {
            throw new RuntimeException("Access denied: You are not a member of this portfolio");
        }
        BigDecimal sum = BigDecimal.ZERO;
        List<Position> positions = positionService.getPositionsForPortfolio(portfolio_id);
        for (Position position : positions) {
            if (position.getSymbol().equals("CASH")){
                sum = sum.add(position.getTotalQuantity());
                continue;
            }
            try {
                BigDecimal current_price = priceQuoteService.getCachedPrice(position.getSymbol(), position.getAssetType());
                sum = sum.add(position.getTotalQuantity().multiply(current_price));
            } catch (Exception ignored) {
                // No market price cached for this position — skip it
            }
        }
        return sum;
    }
    public BigDecimal calculateTotalCost(Long portfolioId) {
        List<Position> positions = positionService.getPositionsForPortfolio(portfolioId);
        BigDecimal sum = BigDecimal.ZERO;
        for (Position position : positions) {
            sum = sum.add(position.getTotalCost());
        }
        return sum;
    }

    public PortfolioSummary getPortfolioSummary(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        BigDecimal pnl = calculatePortfolioPnl(portfolioId);
        BigDecimal totalValue = calculatePortfolioValue(portfolioId);
        BigDecimal totalCost = calculateTotalCost(portfolioId);
        return new PortfolioSummary(portfolio, pnl, totalValue, totalCost);
    }

    public List<Portfolio> findUsersPortfolios(Long user_id) {
        return portfolioRepository.findByCreatedBy_UserId(user_id);
    }

    public List<Portfolio> findAccessiblePortfolios(Long userId) {
        return membershipService.getUserMemberships(userId).stream()
                .map(m -> m.getPortfolio())
                .toList();
    }

    public Portfolio findPortfolioByName(Long userId, String portfolioName){
        return portfolioRepository.findByCreatedBy_UserIdAndName(userId, portfolioName)
                .orElseThrow(
                        () -> new IllegalArgumentException("Portfolio not found with id" + id)
                );

    }


}


