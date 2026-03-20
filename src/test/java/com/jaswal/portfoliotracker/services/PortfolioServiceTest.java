package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PortfolioTrackerApplication.class)
@Transactional
public class PortfolioServiceTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PriceQuoteRepository priceQuoteRepository;

    private User testUser;
    private Portfolio testPortfolio;

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
    }

    @Test
    void testCreatePortfolio_ValidInputs_CreatesPortfolio() {
        String portfolioName = "My Investment Portfolio";

        Portfolio result = portfolioService.createPortfolio(testUser, portfolioName);

        assertNotNull(result);
        assertNotNull(result.getPortfolioId());
        assertEquals(portfolioName, result.getName());
        assertEquals(testUser.getUserId(), result.getCreatedBy().getUserId());
    }

    @Test
    void testCreatePortfolio_WithDifferentUser_CreatesPortfolio() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@test.com");
        anotherUser = userRepository.save(anotherUser);

        Portfolio result = portfolioService.createPortfolio(anotherUser, "Another Portfolio");

        assertNotNull(result);
        assertEquals(anotherUser.getUserId(), result.getCreatedBy().getUserId());
    }

    @Test
    void testGetPortfolio_ExistingId_ReturnsPortfolio() {
        Portfolio result = portfolioService.getPortfolio(testPortfolio.getPortfolioId());

        assertNotNull(result);
        assertEquals(testPortfolio.getPortfolioId(), result.getPortfolioId());
        assertEquals("Test Portfolio", result.getName());
    }

    @Test
    void testGetPortfolio_NonExistingId_ThrowsException() {
        Long nonExistingId = 99999L;

        assertThrows(RuntimeException.class, () -> {
            portfolioService.getPortfolio(nonExistingId);
        });
    }

    @Test
    void testChangePortfolioName_ValidName_UpdatesName() {
        String newName = "Updated Portfolio Name";

        Portfolio result = portfolioService.changePortfolioName(
                testPortfolio.getPortfolioId(),
                newName
        );

        assertNotNull(result);
        assertEquals(newName, result.getName());

        Portfolio fromDb = portfolioRepository.findById(testPortfolio.getPortfolioId()).get();
        assertEquals(newName, fromDb.getName());
    }

    @Test
    void testChangePortfolioName_EmptyName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            portfolioService.changePortfolioName(testPortfolio.getPortfolioId(), "");
        });
    }

    @Test
    void testChangePortfolioName_NullName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            portfolioService.changePortfolioName(testPortfolio.getPortfolioId(), null);
        });
    }

    @Test
    void testChangePortfolioName_WhitespaceName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            portfolioService.changePortfolioName(testPortfolio.getPortfolioId(), "   ");
        });
    }

    @Test
    void testChangePortfolioName_NonExistingPortfolio_ThrowsException() {
        Long nonExistingId = 99999L;

        assertThrows(RuntimeException.class, () -> {
            portfolioService.changePortfolioName(nonExistingId, "New Name");
        });
    }

    @Test
    void testDeletePortfolio_ExistingPortfolio_DeletesSuccessfully() {
        Long portfolioId = testPortfolio.getPortfolioId();

        portfolioService.deletePortfolio(portfolioId);

        assertFalse(portfolioRepository.existsById(portfolioId));
    }

    @Test
    void testDeletePortfolio_NonExistingPortfolio_ThrowsException() {
        Long nonExistingId = 99999L;

        assertThrows(RuntimeException.class, () -> {
            portfolioService.deletePortfolio(nonExistingId);
        });
    }

    @Test
    void testFindUsersPortfolios_UserWithPortfolios_ReturnsPortfolios() {
        Portfolio portfolio2 = new Portfolio();
        portfolio2.setCreatedBy(testUser);
        portfolio2.setName("Second Portfolio");
        portfolioRepository.save(portfolio2);

        Portfolio portfolio3 = new Portfolio();
        portfolio3.setCreatedBy(testUser);
        portfolio3.setName("Third Portfolio");
        portfolioRepository.save(portfolio3);

        List<Portfolio> result = portfolioService.findUsersPortfolios(testUser.getUserId());

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testFindUsersPortfolios_UserWithNoPortfolios_ReturnsEmptyList() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@test.com");
        newUser = userRepository.save(newUser);

        List<Portfolio> result = portfolioService.findUsersPortfolios(newUser.getUserId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindUsersPortfolios_MultipleUsers_ReturnsOnlyUserPortfolios() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@test.com");
        otherUser = userRepository.save(otherUser);

        Portfolio otherPortfolio = new Portfolio();
        otherPortfolio.setCreatedBy(otherUser);
        otherPortfolio.setName("Other User Portfolio");
        portfolioRepository.save(otherPortfolio);

        List<Portfolio> result = portfolioService.findUsersPortfolios(testUser.getUserId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUserId(), result.get(0).getCreatedBy().getUserId());
    }

    @Test
    void testCalculatePortfolioValue_WithPositions_ReturnsCorrectValue() {
        Position position1 = new Position();
        position1.setPortfolio(testPortfolio);
        position1.setSymbol("AAPL");
        position1.setAssetType(AssetType.STOCK);
        position1.setTotalQuantity(BigDecimal.valueOf(10));
        position1.setTotalCost(BigDecimal.valueOf(1500));
        position1.setAverageCostPerUnit(BigDecimal.valueOf(150));
        positionRepository.save(position1);

        Position position2 = new Position();
        position2.setPortfolio(testPortfolio);
        position2.setSymbol("TSLA");
        position2.setAssetType(AssetType.STOCK);
        position2.setTotalQuantity(BigDecimal.valueOf(5));
        position2.setTotalCost(BigDecimal.valueOf(1000));
        position2.setAverageCostPerUnit(BigDecimal.valueOf(200));
        positionRepository.save(position2);

        PriceQuote appleQuote = new PriceQuote();
        appleQuote.setSymbol("AAPL");
        appleQuote.setPrice(BigDecimal.valueOf(175));
        appleQuote.setSource("Test");
        appleQuote.setAssetType(AssetType.STOCK);
        appleQuote.setLastUpdated(LocalDateTime.now());
        priceQuoteRepository.save(appleQuote);

        PriceQuote teslaQuote = new PriceQuote();
        teslaQuote.setSymbol("TSLA");
        teslaQuote.setPrice(BigDecimal.valueOf(250));
        teslaQuote.setSource("Test");
        teslaQuote.setAssetType(AssetType.STOCK);
        teslaQuote.setLastUpdated(LocalDateTime.now());
        priceQuoteRepository.save(teslaQuote);

        BigDecimal result = portfolioService.calculatePortfolioValue(testPortfolio.getPortfolioId());

        BigDecimal expected = BigDecimal.valueOf(3000);
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    void testCalculatePortfolioValue_EmptyPortfolio_ReturnsZero() {
        BigDecimal result = portfolioService.calculatePortfolioValue(testPortfolio.getPortfolioId());

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    void testCalculatePortfolioPnl_WithPositions_ReturnsCorrectPnL() {
        Position position = new Position();
        position.setPortfolio(testPortfolio);
        position.setSymbol("AAPL");
        position.setAssetType(AssetType.STOCK);
        position.setTotalQuantity(BigDecimal.valueOf(10));
        position.setTotalCost(BigDecimal.valueOf(1500));
        position.setAverageCostPerUnit(BigDecimal.valueOf(150));
        positionRepository.save(position);

        PriceQuote quote = new PriceQuote();
        quote.setSymbol("AAPL");
        quote.setPrice(BigDecimal.valueOf(175));
        quote.setSource("Test");
        quote.setAssetType(AssetType.STOCK);
        quote.setLastUpdated(LocalDateTime.now());
        priceQuoteRepository.save(quote);

        BigDecimal result = portfolioService.calculatePortfolioPnl(testPortfolio.getPortfolioId());

        BigDecimal expected = BigDecimal.valueOf(250);
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    void testCalculatePortfolioPnl_WithLoss_ReturnsNegativePnL() {
        Position position = new Position();
        position.setPortfolio(testPortfolio);
        position.setSymbol("TSLA");
        position.setAssetType(AssetType.STOCK);
        position.setTotalQuantity(BigDecimal.valueOf(10));
        position.setTotalCost(BigDecimal.valueOf(2000));
        position.setAverageCostPerUnit(BigDecimal.valueOf(200));
        positionRepository.save(position);

        PriceQuote quote = new PriceQuote();
        quote.setSymbol("TSLA");
        quote.setPrice(BigDecimal.valueOf(150));
        quote.setSource("Test");
        quote.setAssetType(AssetType.STOCK);
        quote.setLastUpdated(LocalDateTime.now());
        priceQuoteRepository.save(quote);

        BigDecimal result = portfolioService.calculatePortfolioPnl(testPortfolio.getPortfolioId());

        BigDecimal expected = BigDecimal.valueOf(-500);
        assertEquals(0, expected.compareTo(result));
    }
}