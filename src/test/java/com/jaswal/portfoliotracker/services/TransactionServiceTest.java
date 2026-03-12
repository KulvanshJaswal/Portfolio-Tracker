package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.enums.TransactionType;
import com.jaswal.portfoliotracker.repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest(classes = PortfolioTrackerApplication.class)
@Transactional
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Portfolio testPortfolio;
    @Autowired
    private PositionService positionService;

    @BeforeEach
    void setUp() {
        // Create fresh test data before each test
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser = userRepository.save(testUser);

        testPortfolio = new Portfolio();
        testPortfolio.setCreatedBy(testUser);
        testPortfolio.setName("Test Portfolio");
        testPortfolio = portfolioRepository.save(testPortfolio);
    }

    @Test
    void testDeposit_NegativeAmount_ThrowsException(){
        BigDecimal negativeAmount = BigDecimal.valueOf(-100.00);

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(testPortfolio.getPortfolioId(), negativeAmount)
        );
    }

    @Test
    void testDeposit_ZeroAmount_ThrowsException() {
        BigDecimal zero = BigDecimal.ZERO;

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(testPortfolio.getPortfolioId(), zero)
        );
    }

    @Test
    void testDeposit_MoreThanTwoDecimals_ThrowsException() {
        BigDecimal moreThanTwoDecimals = BigDecimal.valueOf(100.123);

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(testPortfolio.getPortfolioId(), moreThanTwoDecimals)
        );
    }

    @Test
    void testDeposit_RegularDeposit(){
        BigDecimal regularAmount = BigDecimal.valueOf(500);

        transactionService.deposit(testPortfolio.getPortfolioId(), regularAmount);

        List<Transaction> transactionList = transactionRepository.findByPortfolio(testPortfolio);
        assert(!transactionList.isEmpty());

        Transaction result = transactionList.getFirst();
        assertEquals("quantity check pass", 0, regularAmount.compareTo(result.getQuantity()));
        assertEquals("transaction type pass", TransactionType.DEPOSIT, result.getTransactionType());
    }

    @Test
    void testWithdraw_NegativeAmount_ThrowsException() {
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdrawal(testPortfolio.getPortfolioId(), negativeAmount);
        });
    }

    @Test
    void testWithdraw_ZeroAmount_ThrowsException() {
        BigDecimal zeroAmount = BigDecimal.ZERO;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdrawal(testPortfolio.getPortfolioId(), zeroAmount);
        });
    }

    @Test
    void testWithdraw_MoreThanTwoDecimals_ThrowsException() {
        BigDecimal tooManyDecimals = new BigDecimal("100.123");

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdrawal(testPortfolio.getPortfolioId(), tooManyDecimals);
        });
    }

    @Test
    void testWithdraw_Success_ValidWithdrawal() {
        BigDecimal depositAmount = BigDecimal.valueOf(500);
        BigDecimal withdrawalAmount = BigDecimal.valueOf(100);


        transactionService.deposit(testPortfolio.getPortfolioId(), depositAmount);
        transactionService.withdrawal(testPortfolio.getPortfolioId(), withdrawalAmount);
        List<Transaction> transactionList = transactionRepository.findByPortfolio(testPortfolio);
        assert(!transactionList.isEmpty());
        Transaction result = transactionList.getLast();

        assertEquals("withdrawal transaction success", withdrawalAmount, result.getQuantity());

        Position cashPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                "CASH"
        ).orElseThrow();

        assertEquals("position withdrawal successful",0, new BigDecimal("400").compareTo(cashPosition.getTotalQuantity()));
    }

    @Test
    void testWithdraw_InsufficientFunds_ThrowsException() {
        BigDecimal depositAmount = BigDecimal.valueOf(500);
        BigDecimal withdrawalAmount = BigDecimal.valueOf(600);

        assertThrows(IllegalArgumentException.class, () ->{
            transactionService.withdrawal(testPortfolio.getPortfolioId(), withdrawalAmount);
        });

        transactionService.deposit(testPortfolio.getPortfolioId(), depositAmount);
        assertThrows(IllegalArgumentException.class, () ->{
            transactionService.withdrawal(testPortfolio.getPortfolioId(), withdrawalAmount);
        });
    }

    @Test
    void testBuy_NegativeQuantity_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal negativeQuantity = new BigDecimal(-10);
        AssetType assetType = AssetType.STOCK;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, negativeQuantity, pricePerUnit);
        });

    }

    @Test
    void testBuy_ZeroQuantity_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal zeroQuantity = new BigDecimal(0);
        AssetType assetType = AssetType.STOCK;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, zeroQuantity, pricePerUnit);
        });
    }

    @Test
    void testBuy_QuantityMoreThanEightDecimals_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal decimalQuantity = new BigDecimal("10.123456789");
        AssetType assetType = AssetType.STOCK;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, decimalQuantity, pricePerUnit);
        });
    }

    @Test
    void testBuy_NegativePrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal negativePricePerUnit = new BigDecimal("-150");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, negativePricePerUnit);
        });
    }

    @Test
    void testBuy_ZeroPrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal zeroPricePerUnit = new BigDecimal("0");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, zeroPricePerUnit);
        });
    }

    @Test
    void testBuy_PriceMoreThanEightDecimals_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal decimalPricePerUnit = new BigDecimal("10.123456789");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, decimalPricePerUnit);
        });
    }

    @Test
    void testBuy_InsufficientCash_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(4);
        AssetType assetType = AssetType.STOCK;

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        });
    }

    @Test
    void testBuy_Success_CreatesNewPosition() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(3);
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        List<Transaction> transactionList = transactionRepository.findByPortfolio(testPortfolio);
        Transaction buyTransaction = transactionList.getLast();
        assertEquals("Buy transaction success (transactionType)", TransactionType.BUY, buyTransaction.getTransactionType());

        Position cashPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                "CASH"
        ).orElseThrow();
        assertEquals("Cash position update success", new BigDecimal(50), cashPosition.getTotalQuantity());
        Position applPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                symbol
        ).orElseThrow();
        assertEquals("Apple position created (quantity) success", quantity, applPosition.getTotalQuantity());
        assertEquals("Apple position created (price) success", new BigDecimal(450),applPosition.getTotalCost());
        assertEquals("Apple position created (avg cost per unit) success",  pricePerUnit, applPosition.getAverageCostPerUnit());
    }

    @Test
    void testBuy_Success_AddsToExistingPosition() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        AssetType assetType = AssetType.STOCK;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, new BigDecimal(1), new BigDecimal(150));
        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, new BigDecimal(1), new BigDecimal(250));

        List<Transaction> transactionList = transactionRepository.findByPortfolio(testPortfolio);
        Transaction buyTransaction = transactionList.getLast();
        assertEquals("Buy transaction success (transactionType)", TransactionType.BUY, buyTransaction.getTransactionType());

        Position cashPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                "CASH"
        ).orElseThrow();
        assertEquals("Cash position update success", new BigDecimal(100), cashPosition.getTotalQuantity());
        Position applPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                symbol
        ).orElseThrow();
        assertEquals("Apple position updated (quantity) success", new BigDecimal(2), applPosition.getTotalQuantity());
        assertEquals("Apple position updated (price) success", new BigDecimal(400),applPosition.getTotalCost());
        assertEquals("Apple position updated (avg cost per unit) success",
                new BigDecimal(150).add(new BigDecimal(250)).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP),
                applPosition.getAverageCostPerUnit());
    }
}
