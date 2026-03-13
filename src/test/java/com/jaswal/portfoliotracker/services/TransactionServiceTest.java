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

import static org.junit.jupiter.api.Assertions.*;
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

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(testPortfolio.getPortfolioId(), negativeAmount)
        );
        assertEquals("Exception message", "The amount being deposited has to be greater than zero", exception.getMessage());
    }

    @Test
    void testDeposit_ZeroAmount_ThrowsException() {
        BigDecimal zero = BigDecimal.ZERO;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(testPortfolio.getPortfolioId(), zero)
        );
        assertEquals("Exception message", "The amount being deposited has to be greater than zero", exception.getMessage());
    }

    @Test
    void testDeposit_MoreThanTwoDecimals_ThrowsException() {
        BigDecimal moreThanTwoDecimals = BigDecimal.valueOf(100.123);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(testPortfolio.getPortfolioId(), moreThanTwoDecimals)
        );
        assertEquals("Exception message", "Amount can have a max of 2 decimal places", exception.getMessage());
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

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdrawal(testPortfolio.getPortfolioId(), negativeAmount);
        });
        assertEquals("Exception message", "The amount being withdraw must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdraw_ZeroAmount_ThrowsException() {
        BigDecimal zeroAmount = BigDecimal.ZERO;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdrawal(testPortfolio.getPortfolioId(), zeroAmount);
        });
        assertEquals("Exception message", "The amount being withdraw must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdraw_MoreThanTwoDecimals_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));
        BigDecimal tooManyDecimals = new BigDecimal("100.123");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdrawal(testPortfolio.getPortfolioId(), tooManyDecimals);
        });
        assertEquals("Exception message", "Amount must be a max of 2 decimal places", exception.getMessage());
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

        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->{
            transactionService.withdrawal(testPortfolio.getPortfolioId(), withdrawalAmount);
        });
        assertEquals("Exception message", "Do not have sufficient funds", exception1.getMessage());

        transactionService.deposit(testPortfolio.getPortfolioId(), depositAmount);
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->{
            transactionService.withdrawal(testPortfolio.getPortfolioId(), withdrawalAmount);
        });
        assertEquals("Exception message", "Do not have sufficient funds", exception2.getMessage());
    }

    @Test
    void testBuy_NegativeQuantity_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal negativeQuantity = new BigDecimal(-10);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, negativeQuantity, pricePerUnit);
        });
        assertEquals("Exception message", "The amount being bought should be greater than 0", exception.getMessage());
    }

    @Test
    void testBuy_ZeroQuantity_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal zeroQuantity = new BigDecimal(0);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, zeroQuantity, pricePerUnit);
        });
        assertEquals("Exception message", "The amount being bought should be greater than 0", exception.getMessage());
    }

    @Test
    void testBuy_QuantityMoreThanEightDecimals_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal decimalQuantity = new BigDecimal("10.123456789");
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, decimalQuantity, pricePerUnit);
        });
        assertEquals("Exception message", "The quantity or price per unit has a max of 8 decimal places", exception.getMessage());
    }

    @Test
    void testBuy_NegativePrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal negativePricePerUnit = new BigDecimal("-150");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, negativePricePerUnit);
        });
        assertEquals("Exception message", "The amount being bought should be greater than 0", exception.getMessage());
    }

    @Test
    void testBuy_ZeroPrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal zeroPricePerUnit = new BigDecimal("0");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, zeroPricePerUnit);
        });
        assertEquals("Exception message", "The amount being bought should be greater than 0", exception.getMessage());
    }

    @Test
    void testBuy_PriceMoreThanEightDecimals_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal decimalPricePerUnit = new BigDecimal("10.123456789");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, decimalPricePerUnit);
        });
        assertEquals("Exception message", "The quantity or price per unit has a max of 8 decimal places", exception.getMessage());
    }

    @Test
    void testBuy_InsufficientCash_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(500));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150");
        BigDecimal quantity = new BigDecimal(4);
        AssetType assetType = AssetType.STOCK;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);
        });
        assertEquals("Exception message", "Do not have sufficient funds", exception.getMessage());
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

    @Test
    void testSell_NegativeQuantity_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;
        BigDecimal negativeQuantity = new BigDecimal(-10);

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, negativeQuantity, pricePerUnit);
        });
        assertEquals("Exception message", "The amount being sold should be greater than 0", exception.getMessage());
    }

    @Test
    void testSell_ZeroQuantity_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;
        BigDecimal zeroQuantity = BigDecimal.ZERO;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, zeroQuantity, pricePerUnit);
        });
        assertEquals("Exception message", "The amount being sold should be greater than 0", exception.getMessage());
    }

    @Test
    void testSell_QuantityMoreThanEightDecimals_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;
        BigDecimal decimalQuantity = new BigDecimal("1.123456789");

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, decimalQuantity, pricePerUnit);
        });
        assertEquals("Exception message", "The quantity or price per unit has a max of 8 decimal places", exception.getMessage());
    }

    @Test
    void testSell_NegativePrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;
        BigDecimal negativePricePerUnit = new BigDecimal(-150);

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, quantity, negativePricePerUnit);
        });
        assertEquals("Exception message", "The amount being sold should be greater than 0", exception.getMessage());
    }

    @Test
    void testSell_ZeroPrice_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;
        BigDecimal zeroPricePerUnit = BigDecimal.ZERO;

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, quantity, zeroPricePerUnit);
        });
        assertEquals("Exception message", "The amount being sold should be greater than 0", exception.getMessage());
    }

    @Test
    void testSell_PriceMoreThanEightDecimals_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal(10);
        AssetType assetType = AssetType.STOCK;
        BigDecimal decimalPricePerUnit = new BigDecimal("1.123456789");

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, quantity, decimalPricePerUnit);
        });
        assertEquals("Exception message", "The quantity or price per unit has a max of 8 decimal places", exception.getMessage());
    }

    @Test
    void testSell_InsufficientShares_ThrowsException() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal(50000));

        String symbol = "AAPL";
        BigDecimal pricePerUnit = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal(5);
        AssetType assetType = AssetType.STOCK;
        BigDecimal largerQuantity = new BigDecimal("10");

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, pricePerUnit);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, largerQuantity, pricePerUnit);
        });
        assertEquals("Exception message", "You do not own enough of this asset to sell", exception.getMessage());
    }

    @Test
    void testSell_Success_PartialSale() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal("5000"));

        String symbol = "AAPL";
        AssetType assetType = AssetType.STOCK;
        BigDecimal buyQuantity = new BigDecimal("10");
        BigDecimal buyPrice = new BigDecimal("150");
        BigDecimal sellQuantity = new BigDecimal("5");
        BigDecimal sellPrice = new BigDecimal("200");

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, buyQuantity, buyPrice);
        transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, sellQuantity, sellPrice);

        List<Transaction> transactionList = transactionRepository.findByPortfolio(testPortfolio);
        Transaction sellTransaction = transactionList.getLast();
        assertEquals("Sell transaction type", TransactionType.SELL, sellTransaction.getTransactionType());
        assertEquals("Sell transaction quantity", 0, sellQuantity.compareTo(sellTransaction.getQuantity()));

        Position applPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                symbol
        ).orElseThrow();
        assertEquals("AAPL remaining quantity", 0, new BigDecimal("5").compareTo(applPosition.getTotalQuantity()));
        assertEquals("AAPL remaining cost", 0, new BigDecimal("750").compareTo(applPosition.getTotalCost()));
        assertEquals("AAPL average cost unchanged", 0, buyPrice.compareTo(applPosition.getAverageCostPerUnit()));

        Position cashPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                "CASH"
        ).orElseThrow();
        assertEquals("Cash after sale", 0, new BigDecimal("4500").compareTo(cashPosition.getTotalQuantity()));
    }

    @Test
    void testSell_Success_CompleteExit() {
        transactionService.deposit(testPortfolio.getPortfolioId(), new BigDecimal("5000"));

        String symbol = "AAPL";
        AssetType assetType = AssetType.STOCK;
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal buyPrice = new BigDecimal("150");
        BigDecimal sellPrice = new BigDecimal("200");

        transactionService.buy(testPortfolio.getPortfolioId(), symbol, assetType, quantity, buyPrice);
        transactionService.sell(testPortfolio.getPortfolioId(), symbol, assetType, quantity, sellPrice);

        List<Transaction> transactionList = transactionRepository.findByPortfolio(testPortfolio);
        Transaction sellTransaction = transactionList.getLast();
        assertEquals("Sell transaction type", TransactionType.SELL, sellTransaction.getTransactionType());

        Position applPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                symbol
        ).orElseThrow();
        assertEquals("AAPL sold out - quantity", 0, BigDecimal.ZERO.compareTo(applPosition.getTotalQuantity()));
        assertEquals("AAPL sold out - cost", 0, BigDecimal.ZERO.compareTo(applPosition.getTotalCost()));

        Position cashPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                testPortfolio.getPortfolioId(),
                "CASH"
        ).orElseThrow();
        assertEquals("Cash after complete exit", 0, new BigDecimal("5500").compareTo(cashPosition.getTotalQuantity()));
    }
}