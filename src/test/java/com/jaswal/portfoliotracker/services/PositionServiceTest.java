package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.enums.*;
import com.jaswal.portfoliotracker.repositories.*;
import com.jaswal.portfoliotracker.services.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.Long.parseLong;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;


@SpringBootTest(classes = PortfolioTrackerApplication.class)
@Transactional
public class PositionServiceTest {

    @Autowired
    private PositionService positionService;

    @Autowired
    private TransactionService transactionService;  // You'll need this to create positions!

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Portfolio testPortfolio;
    private User testUser;

    @Autowired
    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser = userRepository.save(testUser);

        testPortfolio = new Portfolio();
        testPortfolio.setCreatedBy(testUser);
        testPortfolio.setName("Test Portfolio");
        testPortfolio = portfolioRepository.save(testPortfolio);

        membershipService.createOwnerMembership(testPortfolio, testUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetPositionsForPortfolio_PortfolioDoesNotExist_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            positionService.getPositionsForPortfolio(parseLong("9999"));
        });
        assertEquals("Exception message correct", "Portfolio does not exist", exception.getMessage());
    }

    @Test
    void testGetPositionsForPortfolio_Success_ReturnsPositions() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        List<Position> positions = positionService.getPositionsForPortfolio(testPortfolio.getPortfolioId());
        for(Position position : positions) {
            if (position.getSymbol().equals("CASH")) {
                assertEquals("Cash position success", new BigDecimal(50), position.getTotalQuantity());
            }
            if (position.getSymbol().equals("AAPL")) {
                assertEquals("Apple position success", quantity,position.getTotalQuantity());
            }
        }
    }

    @Test
    void testGetPositionsForPortfolio_Success_ReturnsEmptyList() {
        List<Position> positions = positionService.getPositionsForPortfolio(testPortfolio.getPortfolioId());
        assertEquals("List is empty success", true, positions.isEmpty());
    }

    @Test
    void testGetPositionBySymbol_PortfolioDoesNotExist_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            positionService.getPositionBySymbol(parseLong("9999"), symbol);
        });
        assertEquals("Exception message correct", "Portfolio does not exist", exception.getMessage());
    }

    @Test
    void testGetPositionBySymbol_SymbolNotFound_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);
        });
        assertEquals("Exception message correct", "Position not found for symbol: " + symbol, exception.getMessage());
    }

    @Test
    void testGetPositionBySymbol_Success_ReturnsPosition() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        Position aaplPosition = positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);

        assertEquals("Obtaining apple position success", symbol, aaplPosition.getSymbol());
    }

    @Test
    void testGetPositionPnl_NullPosition_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        Position aaplPosition = positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
           positionService.getPositionPnl(null, pricePerUnit);
        });

        assertEquals("exception message success","Position does not exist", exception.getMessage());
    }

    @Test
    void testGetPositionPnl_NullPrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        Position aaplPosition = positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            positionService.getPositionPnl(aaplPosition, null);
        });

        assertEquals("exception message correct", "Current price is invalid", exception.getMessage());
    }

    @Test
    void testGetPositionPnl_NegativePrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        Position aaplPosition = positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            positionService.getPositionPnl(aaplPosition, new BigDecimal(-150));
        });

        assertEquals("exception message correct","Current price is invalid", exception.getMessage());
    }

    @Test
    void testGetPositionPnl_Success_Profit() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        Position aaplPosition = positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);

        BigDecimal pnl = positionService.getPositionPnl(aaplPosition, new BigDecimal(200));

        assertEquals("PNL success", new BigDecimal(150), pnl);
    }

    @Test
    void testGetPositionPnl_Success_Loss() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        Position aaplPosition = positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);

        BigDecimal pnl = positionService.getPositionPnl(aaplPosition, new BigDecimal(100));

        assertEquals("PNL success", new BigDecimal(-150), pnl);
    }

    @Test
    void testGetPositionPnl_Success_BreakEven() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        Position aaplPosition = positionService.getPositionBySymbol(testPortfolio.getPortfolioId(), symbol);

        BigDecimal pnl = positionService.getPositionPnl(aaplPosition, new BigDecimal(150));

        assertEquals("PNL success", BigDecimal.ZERO, pnl);
    }
}
