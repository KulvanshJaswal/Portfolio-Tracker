package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.PriceQuote;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.repositories.PriceQuoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PriceQuoteService {

    @Value("${alphavantage.api.key}")
    private String apiKey;
    private final RestClient restClient;
    private final PriceQuoteRepository priceQuoteRepository;
    private final Map<AssetType, Queue<LocalDateTime>> apiCalls = new HashMap<>();


    public PriceQuoteService(PriceQuoteRepository priceQuoteRepository){
        this.priceQuoteRepository = priceQuoteRepository;
        this.restClient = RestClient.create();
    }

    public void cachePrice(String symbol, AssetType assetType, BigDecimal price) {
        PriceQuote quote = priceQuoteRepository.findById(symbol).orElse(new PriceQuote());
        if (quote.getSymbol() == null) {
            quote.setSymbol(symbol);
            quote.setAssetType(assetType);
            quote.setSource("Manual");
        }
        quote.setPrice(price);
        quote.setLastUpdated(LocalDateTime.now());
        priceQuoteRepository.save(quote);
    }

    public BigDecimal getCachedPrice(String symbol, AssetType assetType) {
        if (symbol.equals("CASH")) {
            return BigDecimal.ONE;
        }

        PriceQuote quote = priceQuoteRepository.findById(symbol)
                .orElseThrow(() -> new IllegalArgumentException("No cached price for " + symbol));

        return quote.getPrice();
    }

    public BigDecimal getCurrentPrice(String symbol, AssetType assetType){
        PriceQuote priceQuote = priceQuoteRepository.findById(symbol).orElse(new PriceQuote());

        if (apiCalls.get(assetType) != null && apiCalls.get(assetType).size() >= 25){
            throw new IllegalArgumentException("Max api calls reached");
        }

        String url;
        String outerKey;
        String priceKey;

        if (assetType.equals(AssetType.STOCK)) {
            url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
            outerKey = "Global Quote";
            priceKey = "05. price";
        } else {
            url = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=" + symbol + "&to_currency=USD&apikey=" + apiKey;
            outerKey = "Realtime Currency Exchange Rate";
            priceKey = "5. Exchange Rate";
        }

        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        apiCalls.putIfAbsent(assetType, new ArrayDeque<>());
        apiCalls.get(assetType).add(LocalDateTime.now());

        //Fetching price
        ObjectMapper objectMapper = new ObjectMapper();
        BigDecimal price;
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(response, Map.class);

            Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get(outerKey);
            if (dataMap == null || dataMap.isEmpty()) {
                throw new IllegalArgumentException("Symbol not found: " + symbol);
            }

            String priceString = (String) dataMap.get(priceKey);
            price = new BigDecimal(priceString);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse price data", e);
        }

        //Setting new priceQuote object if not exists
        if(priceQuote.getSymbol() == null){
            priceQuote.setSource("Alpha Vantage");
            priceQuote.setSymbol(symbol);
            priceQuote.setAssetType(assetType);
        }
        priceQuote.setPrice(price);
        priceQuote.setLastUpdated(LocalDateTime.now());
        priceQuoteRepository.save(priceQuote);

        return price;
    }

    @Scheduled(cron = "0 30 16 * * MON-FRI", zone = "America/New_York")
    public void updateAllPrices() {
        List<PriceQuote> allQuotes = priceQuoteRepository.findAll();

        for (PriceQuote quote : allQuotes) {
            try {
                getCurrentPrice(quote.getSymbol(), quote.getAssetType());
                System.out.println("Updated " + quote.getSymbol());
            } catch (Exception e) {
                System.err.println("Failed to update " + quote.getSymbol() + ": " + e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void apiCallCleanUp(){
        for (AssetType assetType : apiCalls.keySet()) {
            Queue<LocalDateTime> queue = apiCalls.get(assetType);

            while(!queue.isEmpty() && queue.peek().isBefore(LocalDateTime.now().minusHours(24))){
                queue.poll();
            }
        }
    }
}
