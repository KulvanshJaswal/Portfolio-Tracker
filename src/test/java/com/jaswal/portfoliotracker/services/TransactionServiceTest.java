package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

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


}
