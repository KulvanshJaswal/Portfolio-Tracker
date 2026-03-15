package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.PortfolioTrackerApplication;
import com.jaswal.portfoliotracker.entities.*;
import com.jaswal.portfoliotracker.enums.*;
import com.jaswal.portfoliotracker.repositories.*;
import com.jaswal.portfoliotracker.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest(classes = PortfolioTrackerApplication.class)
public class PriceQuoteServiceTest {

    @Autowired
    private PriceQuoteService priceQuoteService;

    @Test
    void quickManualTest() {
        // Test stock
        BigDecimal applePrice = priceQuoteService.getCurrentPrice("AAPL", AssetType.STOCK);
        System.out.println("AAPL price: $" + applePrice);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Test crypto
        BigDecimal btcPrice = priceQuoteService.getCurrentPrice("BTC", AssetType.CRYPTO);
        System.out.println("BTC price: $" + btcPrice);

        // If both print valid prices, your service works!
    }
}