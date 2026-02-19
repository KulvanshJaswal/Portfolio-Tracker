package com.jaswal.portfoliotracker.services;

import com.jaswal.portfoliotracker.entities.PriceQuote;
import com.jaswal.portfoliotracker.repositories.PriceQuoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
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

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(response, Map.class);
            Map<String, Object> globalQuote = (Map<String, Object>) jsonMap.get("Global Quote");
            String priceString = (String) globalQuote.get("05. price");
            BigDecimal price = new BigDecimal(priceString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse price data", e);
        }

    }
}
