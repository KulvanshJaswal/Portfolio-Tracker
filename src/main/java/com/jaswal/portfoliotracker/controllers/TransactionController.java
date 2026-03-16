package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.Position;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.services.PositionService;
import com.jaswal.portfoliotracker.services.TransactionService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final PositionService positionService;

    public TransactionController(TransactionService transactionService, PositionService positionService) {
        this.transactionService = transactionService;
        this.positionService = positionService;
    }

    @PostMapping("/deposit")
    public Position deposit(
            @PathVariable Long portfolioId,
            @RequestBody Map<String, BigDecimal> request
    ) {
        BigDecimal amount = request.get("amount");
        transactionService.deposit(portfolioId, amount);

        return positionService.getPositionBySymbol(portfolioId, "CASH");
    }

    @PostMapping("/withdrawal")
    public Position withdrawal(
            @PathVariable Long portfolioId,
            @RequestBody Map<String, BigDecimal> request
    ) {
        BigDecimal amount = request.get("amount");
        transactionService.withdrawal(portfolioId, amount);

        return positionService.getPositionBySymbol(portfolioId, "CASH");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Request {
        private String symbol;
        private AssetType assetType;
        private BigDecimal quantity;
        private BigDecimal pricePerUnit;
    }

    @PostMapping("/buy")
    public List<Position> buy(
            @PathVariable Long portfolioId,
            @RequestBody Request request
    ){
        transactionService.buy(
                portfolioId, request.getSymbol(),
                request.getAssetType(), request.getQuantity(),
                request.getPricePerUnit()
        );

        return List.of(positionService.getPositionBySymbol(portfolioId, request.symbol),
                positionService.getPositionBySymbol(portfolioId, "CASH")
        );
    }

    @PostMapping("/sell")
    public List<Position> sell(
            @PathVariable Long portfolioId,
            @RequestBody Request request
    ){
        transactionService.sell(
                portfolioId, request.getSymbol(),
                request.getAssetType(), request.getQuantity(),
                request.getPricePerUnit()
        );

        return List.of(positionService.getPositionBySymbol(portfolioId, request.symbol),
                positionService.getPositionBySymbol(portfolioId, "CASH")
        );
    }
}