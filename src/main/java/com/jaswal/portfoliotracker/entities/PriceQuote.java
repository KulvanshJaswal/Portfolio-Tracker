package com.jaswal.portfoliotracker.entities;

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
@Table(name = "price_quotes")
public class PriceQuote {
    @Id
    @Column(name = "symbol")
    private String symbol;
    @Column(name = "price", nullable = false)
    private BigDecimal price;
    @Column(name = "source", nullable = false)
    private String source;
    @Column(name = "last_updated", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdated;
}
