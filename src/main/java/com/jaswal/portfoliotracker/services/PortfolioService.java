package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.dto.PortfolioSummary;
import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.Position;
import com.jaswal.portfoliotracker.entities.PriceQuote;
import com.jaswal.portfoliotracker.entities.User;
import com.jaswal.portfoliotracker.repositories.InviteRepository;
import com.jaswal.portfoliotracker.repositories.MembershipRepository;
import com.jaswal.portfoliotracker.repositories.PortfolioRepository;
import com.jaswal.portfoliotracker.repositories.PositionRepository;
import com.jaswal.portfoliotracker.repositories.PriceQuoteRepository;
import com.jaswal.portfoliotracker.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final PriceQuoteService priceQuoteService;
    private final PositionService positionService;
    private final MembershipService membershipService;
    private final InviteRepository inviteRepository;
    private final TransactionRepository transactionRepository;
    private final MembershipRepository membershipRepository;

    public PortfolioService(PortfolioRepository portfolioRepository, PositionRepository positionRepository, PriceQuoteService priceQuoteService, PositionService positionService, MembershipService membershipService, InviteRepository inviteRepository, TransactionRepository transactionRepository, MembershipRepository membershipRepository){
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.positionService = positionService;
        this.priceQuoteService = priceQuoteService;
        this.membershipService = membershipService;
        this.inviteRepository = inviteRepository;
        this.transactionRepository = transactionRepository;
        this.membershipRepository = membershipRepository;
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
    @Transactional
    public void deletePortfolio(Long id){
        membershipService.requireAdmin(id);
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Portfolio not found with id" + id));
        inviteRepository.deleteAllByPortfolio_PortfolioId(id);
        transactionRepository.deleteAllByPortfolio_PortfolioId(id);
        positionRepository.deleteAllByPortfolio_PortfolioId(id);
        membershipRepository.deleteAllByPortfolio_PortfolioId(id);
        portfolioRepository.delete(portfolio);
    }

    public BigDecimal calculatePortfolioPnl(Long portfolio_id){
        membershipService.requireVisitorOrAbove(portfolio_id);
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
        membershipService.requireVisitorOrAbove(portfolio_id);
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
        BigDecimal cashBalance = positionRepository
                .findByPortfolio_PortfolioIdAndSymbol(portfolioId, "CASH")
                .map(Position::getTotalQuantity)
                .orElse(BigDecimal.ZERO);
        return new PortfolioSummary(portfolio, pnl, totalValue, totalCost, cashBalance);
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
                        () -> new IllegalArgumentException("Portfolio not found with name: " + portfolioName)
                );

    }


}


