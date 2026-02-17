package com.jaswal.portfoliotracker.services;


import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.enums.TransactionType;
import com.jaswal.portfoliotracker.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public void buy(Long portfolioId, String symbol, AssetType assetType, BigDecimal quantity, BigDecimal pricePerUnit){
        //Initial Error Checking
        if(quantity.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("The amount being bought should be greater than 0");
        }
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow(
                () -> new IllegalArgumentException("The portfolio does not exist")
        );

        Position cashPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(portfolioId, "CASH").orElseThrow(
                () -> new IllegalArgumentException("Do not have sufficient funds")
        );
        BigDecimal cashAmount = cashPosition.getTotalCost();
        if(cashAmount.compareTo(quantity.multiply(pricePerUnit)) < 0){
            throw new IllegalArgumentException("Do not have sufficient funds");
        }

        //Checks pass -> buying process
        //Make Transaction Record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.BUY);
        transaction.setAssetType(assetType);
        transaction.setPortfolio(portfolio);
        transaction.setQuantity(quantity);
        transaction.setSymbol(symbol);
        transaction.setPricePerUnit(pricePerUnit);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        //Create or update the symbol position
        BigDecimal totalCost = quantity.multiply(pricePerUnit);
        Position symbolPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(portfolioId, symbol).orElse(
                new Position()
        );
        if(symbolPosition.getPositionId() == null){
            symbolPosition.setSymbol(symbol);
            symbolPosition.setPortfolio(portfolio);
            symbolPosition.setAssetType(assetType);
            symbolPosition.setTotalQuantity(quantity);
            symbolPosition.setTotalCost(totalCost);
            symbolPosition.setAverageCostPerUnit(pricePerUnit);
        }
        else{
            symbolPosition.setTotalQuantity(symbolPosition.getTotalQuantity().add(quantity));
            symbolPosition.setTotalCost(symbolPosition.getTotalCost().add(totalCost));
            symbolPosition.setAverageCostPerUnit(symbolPosition.getTotalCost().divide(
                    symbolPosition.getTotalQuantity(), 2, RoundingMode.HALF_UP));
        }

        //Update cash position
        cashPosition.setTotalCost(cashPosition.getTotalCost().subtract(totalCost));
        cashPosition.setTotalQuantity(cashPosition.getTotalQuantity().subtract(totalCost));

        positionRepository.save(cashPosition);
        positionRepository.save(symbolPosition);

    }

    public void sell(Long portfolioId, String symbol, AssetType assetType, BigDecimal quantity, BigDecimal pricePerUnit){
        //Initial error checking
        if(quantity.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("The amount being sold should be greater than 0");
        }
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow(
                () -> new IllegalArgumentException("The portfolio does not exist")
        );

        Position symbolPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(portfolioId, symbol).orElseThrow(
                () -> new IllegalArgumentException("You do not own this asset")
        );
        if(symbolPosition.getTotalQuantity().compareTo(quantity) < 0){
            throw new IllegalArgumentException("You do not own enough of this asset to sell");
        }

        //Checks pass -> selling process
        BigDecimal saleRevenue = quantity.multiply(pricePerUnit);
        BigDecimal costBasisOfSoldShares = quantity.multiply(symbolPosition.getAverageCostPerUnit());

        // Update symbol position
        symbolPosition.setTotalQuantity(symbolPosition.getTotalQuantity().subtract(quantity));
        symbolPosition.setTotalCost(symbolPosition.getTotalCost().subtract(costBasisOfSoldShares));

        // Update cash position
        Position cashPosition = positionRepository.findByPortfolio_PortfolioIdAndSymbol(portfolioId, "CASH").orElseThrow(
                () -> new IllegalStateException("Cash position not found")
        );
        cashPosition.setTotalQuantity(cashPosition.getTotalQuantity().add(saleRevenue));
        cashPosition.setTotalCost(cashPosition.getTotalCost().add(saleRevenue));

        positionRepository.save(symbolPosition);
        positionRepository.save(cashPosition);

        //Make Transaction Record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.SELL);
        transaction.setAssetType(assetType);
        transaction.setPortfolio(portfolio);
        transaction.setQuantity(quantity);
        transaction.setSymbol(symbol);
        transaction.setPricePerUnit(pricePerUnit);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
