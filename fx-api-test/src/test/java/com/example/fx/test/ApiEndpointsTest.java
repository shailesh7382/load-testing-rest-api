package com.example.fx.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Basic API endpoint validation tests
 * These tests ensure that endpoints respond correctly with valid data
 */
public class ApiEndpointsTest {

    @BeforeAll
    public static void setup() {
        String baseUrl = System.getProperty("api.base.url", "http://localhost:8080");
        RestAssured.baseURI = baseUrl;
    }

    @Test
    public void testQuotesEndpoint_ReturnsValidResponse() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/quotes")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testTradesEndpoint_ReturnsValidResponse() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/trades")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testQuotesCountEndpoint_ReturnsNumber() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/quotes/count")
        .then()
            .statusCode(200)
            .body("$", instanceOf(Integer.class));
    }

    @Test
    public void testTradesCountEndpoint_ReturnsNumber() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/trades/count")
        .then()
            .statusCode(200)
            .body("$", instanceOf(Integer.class));
    }

    @Test
    public void testRFQEndpoint_AcceptsValidRequest() {
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
            .body("ask", notNullValue());
    }
}
