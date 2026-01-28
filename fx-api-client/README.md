# FX API Client

This module generates a Java client SDK from the OpenAPI specification (`fx-api/openapi.yaml`).

## Overview

The client-side code generation module uses the OpenAPI Generator Maven Plugin to automatically generate:
- API client classes for all endpoints
- Model classes for all data types
- HTTP client configuration and interceptors

## Generated Code

The generated code is placed in `target/generated-sources/openapi/` and includes:
- **API Classes**: `com.example.fx.client.api.*` - Client methods for each API endpoint
- **Model Classes**: `com.example.fx.client.model.*` - POJOs for Quote, Trade, and other data types
- **Invoker Classes**: `com.example.fx.client.*` - HTTP client configuration

## Building

To generate the client code:

```bash
mvn clean compile
```

The OpenAPI Generator plugin runs during the `generate-sources` phase.

## Usage Example

After building, you can use the generated client in your Java applications:

```java
import com.example.fx.client.ApiClient;
import com.example.fx.client.api.QuotesApi;
import com.example.fx.client.model.Quote;
import com.example.fx.client.model.QuoteInput;

public class Example {
    public static void main(String[] args) {
        // Create API client
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("http://localhost:8080");
        
        // Create Quotes API instance
        QuotesApi quotesApi = new QuotesApi(apiClient);
        
        // Create a new quote
        QuoteInput quoteInput = new QuoteInput()
            .currencyPair("EUR/USD")
            .bid(1.1234)
            .ask(1.1240)
            .mid(1.1237)
            .quoteStatus("ACTIVE");
            
        try {
            Quote createdQuote = quotesApi.createQuote(quoteInput);
            System.out.println("Created quote with ID: " + createdQuote.getId());
            
            // Get all quotes
            List<Quote> quotes = quotesApi.getAllQuotes();
            System.out.println("Total quotes: " + quotes.size());
        } catch (ApiException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

## Configuration

The client generation is configured in `pom.xml`:
- **Generator**: `java`
- **Library**: `okhttp-gson`
- **API Package**: `com.example.fx.client.api`
- **Model Package**: `com.example.fx.client.model`
- **Date Library**: Java 8 date/time API

## Dependencies

The generated client uses:
- **OkHttp** for HTTP communication
- **Gson** for JSON serialization/deserialization
- **Swagger Annotations** for OpenAPI metadata
