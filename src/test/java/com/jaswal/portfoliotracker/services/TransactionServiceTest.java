package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.enums.TransactionType;
import com.jaswal.portfoliotracker.repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

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
}
