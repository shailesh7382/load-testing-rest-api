# FX API Server Stub

This module generates Spring Boot server stubs from the OpenAPI specification (`fx-api/openapi.yaml`).

## Overview

The server-side stub generation module uses the OpenAPI Generator Maven Plugin to automatically generate:
- Spring REST controller interfaces
- Model classes for request/response types
- API documentation annotations

## Generated Code

The generated code is placed in `target/generated-sources/openapi/` and includes:
- **API Interfaces**: `com.example.fx.stub.api.*` - Controller interfaces for each API endpoint
- **Model Classes**: `com.example.fx.stub.model.*` - POJOs for Quote, Trade, and other data types
- **Configuration**: Spring configuration for API endpoints

## Building

To generate the server stubs:

```bash
mvn clean compile
```

The OpenAPI Generator plugin runs during the `generate-sources` phase.

## Usage

The generated interfaces can be implemented in your Spring Boot application:

```java
import com.example.fx.stub.api.QuotesApi;
import com.example.fx.stub.model.Quote;
import com.example.fx.stub.model.QuoteInput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuotesApiController implements QuotesApi {
    
    @Override
    public ResponseEntity<List<Quote>> getAllQuotes() {
        // Your implementation here
        List<Quote> quotes = quoteService.findAll();
        return ResponseEntity.ok(quotes);
    }
    
    @Override
    public ResponseEntity<Quote> createQuote(QuoteInput quoteInput) {
        // Your implementation here
        Quote quote = quoteService.create(quoteInput);
        return ResponseEntity.ok(quote);
    }
    
    // Implement other methods...
}
```

## Key Features

- **Interface-Only Generation**: Generates only interfaces, allowing you to provide custom implementations
- **Spring Boot 3 Compatible**: Uses Spring Boot 3.x annotations and Jakarta EE
- **Validation Support**: Includes Bean Validation annotations on model classes
- **OpenAPI Documentation**: Generated code includes Swagger/OpenAPI annotations

## Configuration

The stub generation is configured in `pom.xml`:
- **Generator**: `spring`
- **API Package**: `com.example.fx.stub.api`
- **Model Package**: `com.example.fx.stub.model`
- **Spring Boot 3**: Enabled
- **Interface Only**: Only generates interfaces, no default implementations

## Benefits

Using generated stubs ensures:
- **API Contract Compliance**: Implementation must match the OpenAPI specification
- **Type Safety**: Compile-time checking of API contracts
- **Reduced Boilerplate**: Auto-generated models and controller interfaces
- **Documentation**: Built-in API documentation from OpenAPI spec
- **Consistency**: Same models and contracts across all services

## Integration with Existing API

The existing `fx-api` module already has a working implementation. This module provides an alternative approach showing how to generate stubs from the OpenAPI spec, which can be useful for:
- Creating new microservices with the same API contract
- Ensuring consistency across multiple service implementations
- Prototyping new API features
