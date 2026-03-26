package com.jaswal.portfoliotracker.controllers;

import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.services.PriceQuoteService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/prices")
public class PriceQuoteController {
    private final PriceQuoteService priceQuoteService;

    public PriceQuoteController(PriceQuoteService priceQuoteService) {
        this.priceQuoteService = priceQuoteService;
    }

    @GetMapping("/{symbol}/live/{assetType}")
    public BigDecimal getLivePrice(@PathVariable String symbol, @PathVariable AssetType assetType) {
        return priceQuoteService.getCurrentPrice(symbol, assetType);
    }

    @GetMapping("/{symbol}/cached/{assetType}")
    public BigDecimal getCachedPrice(@PathVariable String symbol, @PathVariable AssetType assetType) {
        return priceQuoteService.getCachedPrice(symbol, assetType);
    }
}
