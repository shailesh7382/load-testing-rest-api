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

    @PutMapping("/quotes/{id}")
    public Quote updateQuote(@PathVariable Long id, @RequestBody Quote quote) {
        Quote existing = quoteRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        quote.setId(id);
        return quoteRepository.save(quote);
    }

    @DeleteMapping("/quotes/{id}")
    public void deleteQuote(@PathVariable Long id) {
        quoteRepository.deleteById(id);
    }

    @GetMapping("/quotes/currency/{currencyPair}")
    public List<Quote> getQuotesByCurrencyPair(@PathVariable String currencyPair) {
        // URL decode the currency pair to handle slashes
        return quoteRepository.findAll().stream()
                .filter(q -> currencyPair.equals(q.getCurrencyPair()))
                .toList();
    }

    @GetMapping("/quotes/search")
    public List<Quote> searchQuotes(@RequestParam(required = false) String currencyPair,
                                     @RequestParam(required = false) String status) {
        return quoteRepository.findAll().stream()
                .filter(q -> currencyPair == null || currencyPair.equals(q.getCurrencyPair()))
                .filter(q -> status == null || status.equals(q.getQuoteStatus()))
                .toList();
    }

    @GetMapping("/quotes/status/{status}")
    public List<Quote> getQuotesByStatus(@PathVariable String status) {
        return quoteRepository.findAll().stream()
                .filter(q -> status.equals(q.getQuoteStatus()))
                .toList();
    }

    @GetMapping("/quotes/count")
    public long getQuoteCount() {
        return quoteRepository.count();
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

    @PutMapping("/trades/{id}")
    public Trade updateTrade(@PathVariable Long id, @RequestBody Trade trade) {
        Trade existing = tradeRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        trade.setId(id);
        return tradeRepository.save(trade);
    }

    @DeleteMapping("/trades/{id}")
    public void deleteTrade(@PathVariable Long id) {
        tradeRepository.deleteById(id);
    }

    @GetMapping("/trades/currency/{currencyPair}")
    public List<Trade> getTradesByCurrencyPair(@PathVariable String currencyPair) {
        // URL decode the currency pair to handle slashes
        return tradeRepository.findAll().stream()
                .filter(t -> currencyPair.equals(t.getCurrencyPair()))
                .toList();
    }

    @GetMapping("/trades/search")
    public List<Trade> searchTrades(@RequestParam(required = false) String currencyPair,
                                     @RequestParam(required = false) String status) {
        return tradeRepository.findAll().stream()
                .filter(t -> currencyPair == null || currencyPair.equals(t.getCurrencyPair()))
                .filter(t -> status == null || status.equals(t.getStatus()))
                .toList();
    }

    @GetMapping("/trades/status/{status}")
    public List<Trade> getTradesByStatus(@PathVariable String status) {
        return tradeRepository.findAll().stream()
                .filter(t -> status.equals(t.getStatus()))
                .toList();
    }

    @GetMapping("/trades/count")
    public long getTradeCount() {
        return tradeRepository.count();
    }

    @GetMapping("/trades/volume/{currencyPair}")
    public BigDecimal getTradeVolumeByCurrencyPair(@PathVariable String currencyPair) {
        return tradeRepository.findAll().stream()
                .filter(t -> currencyPair.equals(t.getCurrencyPair()))
                .map(Trade::getNotional)
                .filter(notional -> notional != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Simple error response class
    static class ErrorResponse {
        public final String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
    }
}
