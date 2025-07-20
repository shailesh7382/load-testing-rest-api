package com.example.fx.controller;

import com.example.fx.model.Quote;
import com.example.fx.repository.QuoteRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteRepository quoteRepository;
    private final Random random = new Random();

    public QuoteController(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @GetMapping
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    @GetMapping("/{id}")
    public Quote getQuoteById(@PathVariable Long id) {
        return quoteRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Quote createQuote(@RequestBody Quote quote) {
        return quoteRepository.save(quote);
    }

    @PostMapping("/rfq")
    public Quote requestForQuote(@RequestBody Quote rfqRequest) throws InterruptedException {
        // Simulate random delay around 200ms (e.g., 150-250ms)
        Thread.sleep(150 + random.nextInt(100));

        // Generate a randomized quote based on the RFQ request
        Quote quote = new Quote();
        quote.setCurrencyPair(rfqRequest.getCurrencyPair());
        quote.setQuoteProvider("RandomProvider");
        quote.setQuoteTime(LocalDateTime.now());
        quote.setVenue("Venue" + (random.nextInt(5) + 1));
        quote.setLiquidityProvider("LP" + (random.nextInt(3) + 1));
        quote.setQuoteId("Q" + (10000 + random.nextInt(90000)));
        quote.setTenor(rfqRequest.getTenor() != null ? rfqRequest.getTenor() : "SPOT");
        quote.setSettlementType("T+2");
        quote.setQuoteStatus("ACTIVE");
        quote.setSourceSystem("RFQ-API");
        quote.setPricingModel("Model" + (random.nextInt(3) + 1));
        quote.setPriceType("Firm");
        quote.setMarketDataSource("Market" + (random.nextInt(3) + 1));
        quote.setQuoteCondition("Normal");
        quote.setQuoteOrigin("Auto");
        quote.setQuoteType("Indicative");
        quote.setQuoteLevel("Level" + (random.nextInt(2) + 1));
        quote.setQuoteSide("Buy");
        quote.setQuoteChannel("API");
        quote.setQuoteVersion("1");
        quote.setQuoteReference("Ref" + (random.nextInt(1000) + 1));
        quote.setQuoteComment("Auto-generated RFQ quote");

        // Randomize bid/ask/mid
        double base = 1.10 + random.nextDouble() * 0.1; // e.g., 1.10 - 1.20
        double spread = 0.0005 + random.nextDouble() * 0.001; // e.g., 0.0005 - 0.0015
        BigDecimal bid = BigDecimal.valueOf(base);
        BigDecimal ask = BigDecimal.valueOf(base + spread);
        BigDecimal mid = BigDecimal.valueOf((bid.doubleValue() + ask.doubleValue()) / 2.0);

        quote.setBid(bid);
        quote.setAsk(ask);
        quote.setMid(mid);

        // Save and return the quote
        return quoteRepository.save(quote);
    }
}
