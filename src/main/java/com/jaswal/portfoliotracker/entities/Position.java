package com.jaswal.portfoliotracker.entities;

import com.jaswal.portfoliotracker.enums.AssetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "positions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "portfolio_id"}) )
public class Position {
    @Id
    @Column(name = "position_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long positionId;
    @ManyToOne()
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
    @Column(name = "symbol", length = 10)
    private String symbol;
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;
    @Column(name = "total_quantity", nullable = false)
    private BigDecimal totalQuantity;
    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;
    @Column(name = "average_cost_per_unit", nullable = false)
    private BigDecimal averageCostPerUnit;
}
