package com.example.fx.test;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/**
 * OpenAPI Schema validation tests
 * These tests ensure that API requests and responses conform to the OpenAPI specification
 */
public class OpenApiValidationTest {

    private static OpenApiValidationFilter validationFilter;

    @BeforeAll
    public static void setup() {
        String baseUrl = System.getProperty("api.base.url", "http://localhost:8080");
        RestAssured.baseURI = baseUrl;

        // Get OpenAPI spec path from system property or use default
        String specPath = System.getProperty("openapi.spec.path");
        if (specPath == null || specPath.isEmpty()) {
            // Default to relative path from module root
            specPath = Paths.get("../fx-api/openapi.yaml").toAbsolutePath().toString();
        }

        // Create validation filter
        validationFilter = new OpenApiValidationFilter(specPath);
    }

    @Test
    public void testQuoteCreation_ValidatesAgainstSchema() {
        String quotePayload = """
            {
              "currencyPair": "EUR/USD",
              "bid": 1.1234,
              "ask": 1.1240,
              "mid": 1.1237,
              "quoteProvider": "ProviderX",
              "quoteTime": "2024-06-01T12:34:56",
              "quoteStatus": "ACTIVE"
            }
            """;

        given()
            .filter(validationFilter)
            .contentType(ContentType.JSON)
            .body(quotePayload)
        .when()
            .post("/api/quotes")
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    public void testTradeCreation_ValidatesAgainstSchema() {
        String tradePayload = """
            {
              "tradeId": "T12345",
              "currencyPair": "EUR/USD",
              "notional": 1000000,
              "direction": "BUY",
              "price": 1.1237,
              "status": "CONFIRMED"
            }
            """;

        given()
            .filter(validationFilter)
            .contentType(ContentType.JSON)
            .body(tradePayload)
        .when()
            .post("/api/trades")
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    public void testGetAllQuotes_ResponseValidatesAgainstSchema() {
        given()
            .filter(validationFilter)
            .accept(ContentType.JSON)
        .when()
            .get("/api/quotes")
        .then()
            .statusCode(200);
    }

    @Test
    public void testGetAllTrades_ResponseValidatesAgainstSchema() {
        given()
            .filter(validationFilter)
            .accept(ContentType.JSON)
        .when()
            .get("/api/trades")
        .then()
            .statusCode(200);
    }

    @Test
    public void testRFQ_ValidatesAgainstSchema() {
        String rfqPayload = """
            {
              "currencyPair": "EUR/USD",
              "tenor": "SPOT"
            }
            """;

        given()
            .filter(validationFilter)
            .contentType(ContentType.JSON)
            .body(rfqPayload)
        .when()
            .post("/api/quotes/rfq")
        .then()
            .statusCode(200)
            .body("currencyPair", notNullValue());
    }
}
