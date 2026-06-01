package com.jaswal.portfoliotracker.repositories;

import com.jaswal.portfoliotracker.entities.Portfolio;
import com.jaswal.portfoliotracker.entities.Transaction;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByPortfolio(Portfolio portfolio);

    List<Transaction> findBySymbol(String symbol);

    List<Transaction> findByTransactionType(TransactionType transactionType);

    List<Transaction> findBySymbolAndPortfolio(String symbol, Portfolio portfolio);


    List<Transaction> findBySymbolAndTransactionType(String symbol, TransactionType transactionType);


    List<Transaction> findByAssetTypeAndPortfolio(AssetType assetType, Portfolio portfolio);

    void deleteAllByPortfolio_PortfolioId(Long portfolioId);







}
