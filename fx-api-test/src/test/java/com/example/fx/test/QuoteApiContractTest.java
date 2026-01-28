package com.example.fx.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Contract tests for Quote API endpoints
 * These tests validate that the API implementation conforms to the OpenAPI specification
 */
public class QuoteApiContractTest {

    @BeforeAll
    public static void setup() {
        String baseUrl = System.getProperty("api.base.url", "http://localhost:8080");
        RestAssured.baseURI = baseUrl;
    }

    @Test
    public void testGetAllQuotes_ReturnsArray() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/quotes")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    public void testCreateQuote_ValidPayload_ReturnsQuote() {
        String quotePayload = """
            {
              "currencyPair": "EUR/USD",
              "bid": 1.1234,
              "ask": 1.1240,
              "mid": 1.1237,
              "quoteProvider": "ProviderX",
              "quoteTime": "2024-06-01T12:34:56",
              "venue": "VenueA",
              "liquidityProvider": "LP1",
              "quoteId": "Q12345",
              "tenor": "SPOT",
              "settlementType": "T+2",
              "quoteStatus": "ACTIVE"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(quotePayload)
        .when()
            .post("/api/quotes")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("currencyPair", equalTo("EUR/USD"))
            .body("bid", equalTo(1.1234f))
            .body("ask", equalTo(1.1240f));
    }

    @Test
    public void testGetQuoteById_ExistingId_ReturnsQuote() {
        // First create a quote
        String quotePayload = """
            {
              "currencyPair": "GBP/USD",
              "bid": 1.2500,
              "ask": 1.2510,
              "mid": 1.2505,
              "quoteStatus": "ACTIVE"
            }
            """;

        Integer quoteId = given()
            .contentType(ContentType.JSON)
            .body(quotePayload)
        .when()
            .post("/api/quotes")
        .then()
            .statusCode(200)
            .extract()
            .path("id");

        // Then retrieve it by ID
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/quotes/{id}", quoteId)
        .then()
            .statusCode(200)
            .body("id", equalTo(quoteId))
            .body("currencyPair", equalTo("GBP/USD"));
    }

    @Test
    public void testGetQuotesByCurrencyPair_ReturnsFilteredQuotes() {
        // Create a quote with specific currency pair
        String quotePayload = """
            {
              "currencyPair": "USD/JPY",
              "bid": 110.50,
              "ask": 110.60,
              "mid": 110.55,
              "quoteStatus": "ACTIVE"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(quotePayload)
        .when()
            .post("/api/quotes")
        .then()
            .statusCode(200);

        // Retrieve quotes by currency pair
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/quotes/currency/{currencyPair}", "USD/JPY")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].currencyPair", equalTo("USD/JPY"));
    }

    @Test
    public void testGetQuoteCount_ReturnsNumber() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/quotes/count")
        .then()
            .statusCode(200)
            .body("$", instanceOf(Integer.class));
    }

    @Test
    public void testRequestForQuote_ValidRequest_ReturnsQuote() {
        String rfqPayload = """
            {
              "currencyPair": "EUR/USD",
              "tenor": "SPOT"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(rfqPayload)
        .when()
            .post("/api/quotes/rfq")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("currencyPair", equalTo("EUR/USD"))
            .body("bid", notNullValue())
            .body("ask", notNullValue())
            .body("mid", notNullValue());
    }

    @Test
    public void testSearchQuotes_WithFilters_ReturnsFilteredQuotes() {
        given()
            .accept(ContentType.JSON)
            .queryParam("status", "ACTIVE")
        .when()
            .get("/api/quotes/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", instanceOf(java.util.List.class));
    }
}
