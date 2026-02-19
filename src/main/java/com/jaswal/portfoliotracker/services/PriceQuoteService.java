package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.PriceQuote;
import com.jaswal.portfoliotracker.repositories.PriceQuoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public BigDecimal getPrice(String symbol){
        //Initial error checking
        PriceQuote priceQuote = priceQuoteRepository.findById(symbol).orElse(new PriceQuote());
        String response = restClient.get()
                .uri("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey)
                .retrieve()
                .body(String.class);

        //Fetching price
        ObjectMapper objectMapper = new ObjectMapper();
        BigDecimal price;
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(response, Map.class);
            Map<String, Object> globalQuote = (Map<String, Object>) jsonMap.get("Global Quote");
            String priceString = (String) globalQuote.get("05. price");
            price = new BigDecimal(priceString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse price data", e);
        }
        //Setting new priceQuote object if not exists
        if(priceQuote.getSymbol() == null){
            priceQuote.setSource("Alpha Vantage");
            priceQuote.setSymbol(symbol);
        }
        priceQuote.setPrice(price);
        priceQuote.setLastUpdated(LocalDateTime.now());
        priceQuoteRepository.save(priceQuote);

        return price;
    }
}
