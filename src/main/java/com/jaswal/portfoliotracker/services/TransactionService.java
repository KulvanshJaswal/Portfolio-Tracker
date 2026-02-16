package com.jaswal.portfoliotracker.services;


import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.enums.TransactionType;
import com.jaswal.portfoliotracker.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
                () -> new IllegalArgumentException("Portfolio does not exist.")
        );

        //Make Transaction Record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAssetType(AssetType.CASH);
        transaction.setPortfolio(portfolio);
        transaction.setQuantity(amount);
        transaction.setSymbol("CASH");
        transaction.setPricePerUnit(BigDecimal.ONE);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        //Make Position Record
        Position position = positionRepository.findByPortfolio_PortfolioIdAndSymbol(
                portfolioId, "CASH").orElse(new Position());
        if (position.getPositionId()== null) {
            //set up all the fields
            position.setPortfolio(portfolio);
            position.setSymbol("CASH");
            position.setAssetType(AssetType.CASH);
            position.setTotalQuantity(amount);
            position.setTotalCost(amount);
            position.setAverageCostPerUnit(BigDecimal.ONE);
        }
        else {
            //just add to the quantity
            position.setTotalQuantity(position.getTotalQuantity().add(amount));
            position.setTotalCost(position.getTotalCost().add(amount));
        }
        positionRepository.save(position);
    }

    public void withdrawal(Long portfolioId, BigDecimal amount){
        //Initial error checking
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("The amount being withdraw must be greater than zero");
        }
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow(
                () -> new IllegalArgumentException("Portfolio does not exist")
        );
        //Checking cash
        Position position = positionRepository.findByPortfolio_PortfolioIdAndSymbol(portfolioId, "CASH").orElseThrow(
                () -> new IllegalArgumentException("Do not have sufficient funds")
        );
        BigDecimal userAmount = position.getTotalQuantity();
        if(userAmount.compareTo(amount) < 0){
            throw new IllegalArgumentException("Do not have sufficient funds");
        }

        //Have sufficient - edit position
        position.setTotalCost(position.getTotalCost().subtract(amount));
        position.setTotalQuantity(position.getTotalQuantity().subtract(amount));
        positionRepository.save(position);

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAssetType(AssetType.CASH);
        transaction.setPortfolio(portfolio);
        transaction.setQuantity(amount);
        transaction.setSymbol("CASH");
        transaction.setPricePerUnit(BigDecimal.ONE);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

    }

}
