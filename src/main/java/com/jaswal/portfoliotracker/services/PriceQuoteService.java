package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.PriceQuote;
import com.jaswal.portfoliotracker.enums.AssetType;
import com.jaswal.portfoliotracker.repositories.PriceQuoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class PriceQuoteService {

    @Value("${alphavantage.api.key}")
    private String apiKey;
    private final RestClient restClient;
    private final PriceQuoteRepository priceQuoteRepository;


    public PriceQuoteService(PriceQuoteRepository priceQuoteRepository){
        this.priceQuoteRepository = priceQuoteRepository;
        this.restClient = RestClient.create();
    }

    public BigDecimal getCurrentPrice(String symbol, AssetType assetType){
        //Initial setup
        PriceQuote priceQuote = priceQuoteRepository.findById(symbol).orElse(new PriceQuote());

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

    public void updateAll(){
        //Find all price quote objects
        List<PriceQuote> priceQuotes = priceQuoteRepository.findAll();

        for(PriceQuote priceQuote : priceQuotes){
            getCurrentPrice(priceQuote.getSymbol(), priceQuote.getAssetType());
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
