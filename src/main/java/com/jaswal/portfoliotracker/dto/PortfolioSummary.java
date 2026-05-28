package com.jaswal.portfoliotracker.dto;

import com.jaswal.portfoliotracker.entities.Portfolio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummary {
    private Portfolio portfolio;
    private BigDecimal pnl;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal cashBalance;
}
