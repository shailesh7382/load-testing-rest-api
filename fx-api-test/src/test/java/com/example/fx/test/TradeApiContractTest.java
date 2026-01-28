package com.example.fx.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Contract tests for Trade API endpoints
 * These tests validate that the API implementation conforms to the OpenAPI specification
 */
public class TradeApiContractTest {

    @BeforeAll
    public static void setup() {
        String baseUrl = System.getProperty("api.base.url", "http://localhost:8080");
        RestAssured.baseURI = baseUrl;
    }

    @Test
    public void testGetAllTrades_ReturnsArray() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/trades")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    public void testCreateTrade_ValidPayload_ReturnsTrade() {
        String tradePayload = """
            {
              "tradeId": "T12345",
              "currencyPair": "EUR/USD",
              "notional": 1000000,
              "direction": "BUY",
              "price": 1.1237,
              "counterparty": "BankA",
              "tradeDateTime": "2024-06-01T12:35:00",
              "trader": "Trader1",
              "book": "BookA",
              "status": "CONFIRMED",
              "settlementType": "T+2",
              "settlementDate": "2024-06-03",
              "tradeType": "SPOT"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(tradePayload)
        .when()
            .post("/api/trades")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("currencyPair", equalTo("EUR/USD"))
            .body("notional", instanceOf(Number.class))
            .body("direction", equalTo("BUY"));
    }

    @Test
    public void testGetTradeById_ExistingId_ReturnsTrade() {
        // First create a trade
        String tradePayload = """
            {
              "tradeId": "T67890",
              "currencyPair": "GBP/USD",
              "notional": 500000,
              "direction": "SELL",
              "price": 1.2505,
              "status": "CONFIRMED"
            }
            """;

        Integer tradeId = given()
            .contentType(ContentType.JSON)
            .body(tradePayload)
        .when()
            .post("/api/trades")
        .then()
            .statusCode(200)
            .extract()
            .path("id");

        // Then retrieve it by ID
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/trades/{id}", tradeId)
        .then()
            .statusCode(200)
            .body("id", equalTo(tradeId))
            .body("currencyPair", equalTo("GBP/USD"));
    }

    @Test
    public void testGetTradesByCurrencyPair_ReturnsFilteredTrades() {
        // Create a trade with specific currency pair
        String tradePayload = """
            {
              "tradeId": "T99999",
              "currencyPair": "USD/JPY",
              "notional": 750000,
              "direction": "BUY",
              "price": 110.55,
              "status": "CONFIRMED"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(tradePayload)
        .when()
            .post("/api/trades")
        .then()
            .statusCode(200);

        // Retrieve trades by currency pair using search endpoint
        // Note: Using search endpoint instead of path parameter due to URL encoding limitations with slashes
        given()
            .accept(ContentType.JSON)
            .queryParam("currencyPair", "USD/JPY")
        .when()
            .get("/api/trades/search")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].currencyPair", equalTo("USD/JPY"));
    }

    @Test
    public void testGetTradeCount_ReturnsNumber() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/trades/count")
        .then()
            .statusCode(200)
            .body("$", instanceOf(Integer.class));
    }

    @Test
    public void testGetTradeVolume_ReturnsTotalVolume() {
        // Create a trade with a simple currency pair (without slash to avoid URL encoding issues)
        String tradePayload = """
            {
              "tradeId": "T88888",
              "currencyPair": "EURGBP",
              "notional": 2000000,
              "direction": "BUY",
              "price": 0.85,
              "status": "CONFIRMED"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(tradePayload)
        .when()
            .post("/api/trades")
        .then()
            .statusCode(200);

        // Get volume for that currency pair
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/trades/volume/{currencyPair}", "EURGBP")
        .then()
            .statusCode(200)
            .body("$", instanceOf(Number.class))
            .body("$", greaterThanOrEqualTo(2000000.0f));
    }

    @Test
    public void testSearchTrades_WithFilters_ReturnsFilteredTrades() {
        given()
            .accept(ContentType.JSON)
            .queryParam("status", "CONFIRMED")
        .when()
            .get("/api/trades/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", instanceOf(java.util.List.class));
    }
}
