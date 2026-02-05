package com.jaswal.portfoliotracker.entities;

import com.jaswal.portfoliotracker.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    @ManyToOne()
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;
    @Column(name = "symbol", nullable = false)
    private String symbol;
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;
    @Column(name = "price_per_unit", nullable = false)
    private BigDecimal pricePerUnit;
    @Column(name = "timestamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime timestamp;
}
