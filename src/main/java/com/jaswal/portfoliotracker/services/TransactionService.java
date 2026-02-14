package com.jaswal.portfoliotracker.services;


import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Transactional
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository;

    public TransactionService(TransactionRepository transactionRepository, PositionRepository positionRepository, PortfolioRepository portfolioRepository){
        this.transactionRepository = transactionRepository;
        this.positionRepository = positionRepository;
        this.portfolioRepository =portfolioRepository;
    }

    public void deposit(Long portfolioId, BigDecimal amount){
        //Initial Error Checking
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("The amount being deposited has to be greater than zero");
        }
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow(
                () -> new IllegalArgumentException("Portfolio does not exist."));

        //Make Transaction Record
        Transaction transaction = new Transaction(portfolioId, "DEPOSIT", "CASH", "CASH", amount, BigDecimal.ONE);

    }


}
