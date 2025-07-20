package com.example.fx.controller;

import com.example.fx.model.Quote;
import com.example.fx.model.Trade;
import com.example.fx.repository.QuoteRepository;
import com.example.fx.repository.TradeRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class FxController {

    private final QuoteRepository quoteRepository;
    private final TradeRepository tradeRepository;
    private final Random random = new Random();

    public FxController(QuoteRepository quoteRepository, TradeRepository tradeRepository) {
        this.quoteRepository = quoteRepository;
        this.tradeRepository = tradeRepository;
    }

    // --- Quote endpoints ---

    @GetMapping("/quotes")
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    @GetMapping("/quotes/{id}")
    public Quote getQuoteById(@PathVariable Long id) {
        return quoteRepository.findById(id).orElse(null);
    }

    @PostMapping("/quotes")
    public Quote createQuote(@RequestBody Quote quote) {
        return quoteRepository.save(quote);
    }

    @PostMapping("/quotes/rfq")
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

    // --- Trade endpoints ---

    @GetMapping("/trades")
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    @GetMapping("/trades/{id}")
    public Trade getTradeById(@PathVariable Long id) {
        return tradeRepository.findById(id).orElse(null);
    }

    @PostMapping("/trades")
    public Object createTrade(@RequestBody Trade trade) {
        // Validate quoteId exists if provided
        String quoteId = trade.getQuoteId();
        if (quoteId != null && !quoteId.isEmpty()) {
            Optional<Quote> quoteOpt = quoteRepository.findAll().stream()
                    .filter(q -> quoteId.equals(q.getQuoteId()))
                    .findFirst();
            if (!quoteOpt.isPresent()) {
                return new ErrorResponse("Invalid quoteId: " + quoteId);
            }
        }
        return tradeRepository.save(trade);
    }

    // Simple error response class
    static class ErrorResponse {
        public final String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
    }
}
