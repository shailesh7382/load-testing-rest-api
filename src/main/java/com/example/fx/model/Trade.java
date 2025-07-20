package com.example.fx.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tradeId;
    private String currencyPair;
    private BigDecimal notional;
    private String direction;
    private BigDecimal price;
    private String counterparty;
    private LocalDateTime tradeDateTime;
    private String trader;
    private String book;
    private String status;
    private String settlementType;
    private String settlementDate;
    private String tradeType;
    private String executionVenue;
    private String sourceSystem;
    private String tradeReference;
    private String tradeComment;
    private String tradeVersion;
    private String tradeChannel;
    private String tradeStrategy;
    private String tradeDesk;
    private String tradeOrigin;
    private String tradeLevel;
    private String tradeSide;
    private String tradeCondition;
    private String tradeCategory;
    private String tradeSubType;
    private String tradeBookType;

    private String quoteId; // Reference to Quote

    // Getters and setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getNotional() {
        return notional;
    }

    public void setNotional(BigDecimal notional) {
        this.notional = notional;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public LocalDateTime getTradeDateTime() {
        return tradeDateTime;
    }

    public void setTradeDateTime(LocalDateTime tradeDateTime) {
        this.tradeDateTime = tradeDateTime;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getExecutionVenue() {
        return executionVenue;
    }

    public void setExecutionVenue(String executionVenue) {
        this.executionVenue = executionVenue;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getTradeReference() {
        return tradeReference;
    }

    public void setTradeReference(String tradeReference) {
        this.tradeReference = tradeReference;
    }

    public String getTradeComment() {
        return tradeComment;
    }

    public void setTradeComment(String tradeComment) {
        this.tradeComment = tradeComment;
    }

    public String getTradeVersion() {
        return tradeVersion;
    }

    public void setTradeVersion(String tradeVersion) {
        this.tradeVersion = tradeVersion;
    }

    public String getTradeChannel() {
        return tradeChannel;
    }

    public void setTradeChannel(String tradeChannel) {
        this.tradeChannel = tradeChannel;
    }

    public String getTradeStrategy() {
        return tradeStrategy;
    }

    public void setTradeStrategy(String tradeStrategy) {
        this.tradeStrategy = tradeStrategy;
    }

    public String getTradeDesk() {
        return tradeDesk;
    }

    public void setTradeDesk(String tradeDesk) {
        this.tradeDesk = tradeDesk;
    }

    public String getTradeOrigin() {
        return tradeOrigin;
    }

    public void setTradeOrigin(String tradeOrigin) {
        this.tradeOrigin = tradeOrigin;
    }

    public String getTradeLevel() {
        return tradeLevel;
    }

    public void setTradeLevel(String tradeLevel) {
        this.tradeLevel = tradeLevel;
    }

    public String getTradeSide() {
        return tradeSide;
    }

    public void setTradeSide(String tradeSide) {
        this.tradeSide = tradeSide;
    }

    public String getTradeCondition() {
        return tradeCondition;
    }

    public void setTradeCondition(String tradeCondition) {
        this.tradeCondition = tradeCondition;
    }

    public String getTradeCategory() {
        return tradeCategory;
    }

    public void setTradeCategory(String tradeCategory) {
        this.tradeCategory = tradeCategory;
    }

    public String getTradeSubType() {
        return tradeSubType;
    }

    public void setTradeSubType(String tradeSubType) {
        this.tradeSubType = tradeSubType;
    }

    public String getTradeBookType() {
        return tradeBookType;
    }

    public void setTradeBookType(String tradeBookType) {
        this.tradeBookType = tradeBookType;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }
}
