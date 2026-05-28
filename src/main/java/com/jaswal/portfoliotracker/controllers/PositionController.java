package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.entities.Position;
import com.jaswal.portfoliotracker.services.PositionService;
import com.jaswal.portfoliotracker.services.PriceQuoteService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/positions")
public class PositionController {

    private PositionService positionService;
    private PriceQuoteService priceQuoteService;

    public PositionController(PositionService positionService,  PriceQuoteService priceQuoteService) {
        this.positionService = positionService;
        this.priceQuoteService = priceQuoteService;
    }

    @GetMapping("")
    public List<Position> getAllPositions(@PathVariable Long portfolioId){
        return positionService.getPositionsForPortfolio(portfolioId);
    }

    @GetMapping("/{symbol}")
    public Position getPosition(@PathVariable Long portfolioId, @PathVariable String symbol){
        return positionService.getPositionBySymbol(portfolioId, symbol);
    }

    @GetMapping("/{symbol}/pnl")
    public BigDecimal getPositionPnl(@PathVariable Long portfolioId, @PathVariable String symbol){
        Position position = positionService.getPositionBySymbol(portfolioId, symbol);
        BigDecimal currentPrice = priceQuoteService.getCachedPrice(symbol, position.getAssetType());
        return positionService.getPositionPnl(position, currentPrice);
    }

}
