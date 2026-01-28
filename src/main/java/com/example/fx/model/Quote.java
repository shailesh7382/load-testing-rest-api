package com.example.fx.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyPair;
    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal mid;
    private String quoteProvider;
    private LocalDateTime quoteTime;
    private String venue;
    private String liquidityProvider;
    private String quoteId;
    private String tenor;
    private String settlementType;
    private String quoteStatus;
    private String sourceSystem;
    private String pricingModel;
    private String priceType;
    private String marketDataSource;
    private String quoteCondition;
    private String quoteOrigin;
    private String quoteType;
    private String quoteLevel;
    private String quoteSide;
    private String quoteChannel;
    private String quoteVersion;
    private String quoteReference;
    private String quoteComment;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public BigDecimal getMid() {
        return mid;
    }

    public void setMid(BigDecimal mid) {
        this.mid = mid;
    }

    public String getQuoteProvider() {
        return quoteProvider;
    }

    public void setQuoteProvider(String quoteProvider) {
        this.quoteProvider = quoteProvider;
    }

    public LocalDateTime getQuoteTime() {
        return quoteTime;
    }

    public void setQuoteTime(LocalDateTime quoteTime) {
        this.quoteTime = quoteTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getLiquidityProvider() {
        return liquidityProvider;
    }

    public void setLiquidityProvider(String liquidityProvider) {
        this.liquidityProvider = liquidityProvider;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public String getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getPricingModel() {
        return pricingModel;
    }

    public void setPricingModel(String pricingModel) {
        this.pricingModel = pricingModel;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getMarketDataSource() {
        return marketDataSource;
    }

    public void setMarketDataSource(String marketDataSource) {
        this.marketDataSource = marketDataSource;
    }

    public String getQuoteCondition() {
        return quoteCondition;
    }

    public void setQuoteCondition(String quoteCondition) {
        this.quoteCondition = quoteCondition;
    }

    public String getQuoteOrigin() {
        return quoteOrigin;
    }

    public void setQuoteOrigin(String quoteOrigin) {
        this.quoteOrigin = quoteOrigin;
    }

    public String getQuoteType() {
        return quoteType;
    }

    public void setQuoteType(String quoteType) {
        this.quoteType = quoteType;
    }

    public String getQuoteLevel() {
        return quoteLevel;
    }

    public void setQuoteLevel(String quoteLevel) {
        this.quoteLevel = quoteLevel;
    }

    public String getQuoteSide() {
        return quoteSide;
    }

    public void setQuoteSide(String quoteSide) {
        this.quoteSide = quoteSide;
    }

    public String getQuoteChannel() {
        return quoteChannel;
    }

    public void setQuoteChannel(String quoteChannel) {
        this.quoteChannel = quoteChannel;
    }

    public String getQuoteVersion() {
        return quoteVersion;
    }

    public void setQuoteVersion(String quoteVersion) {
        this.quoteVersion = quoteVersion;
    }

    public String getQuoteReference() {
        return quoteReference;
    }

    public void setQuoteReference(String quoteReference) {
        this.quoteReference = quoteReference;
    }

    public String getQuoteComment() {
        return quoteComment;
    }

    public void setQuoteComment(String quoteComment) {
        this.quoteComment = quoteComment;
    }
}
