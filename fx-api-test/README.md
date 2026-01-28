# FX API Testing

This module provides comprehensive API testing and validation using the OpenAPI specification.

## Overview

This module contains:
- **Contract Tests**: Validate that the API implementation conforms to the OpenAPI specification
- **Integration Tests**: End-to-end testing of API endpoints
- **OpenAPI Validation**: Automated validation of requests and responses against the schema

## Test Classes

### QuoteApiContractTest
Tests all Quote API endpoints:
- GET /api/quotes - List all quotes
- POST /api/quotes - Create a new quote
- GET /api/quotes/{id} - Get quote by ID
- GET /api/quotes/currency/{currencyPair} - Filter quotes by currency pair
- GET /api/quotes/count - Get quote count
- POST /api/quotes/rfq - Request for quote
- GET /api/quotes/search - Search quotes with filters

### TradeApiContractTest
Tests all Trade API endpoints:
- GET /api/trades - List all trades
- POST /api/trades - Book a new trade
- GET /api/trades/{id} - Get trade by ID
- GET /api/trades/currency/{currencyPair} - Filter trades by currency pair
- GET /api/trades/count - Get trade count
- GET /api/trades/volume/{currencyPair} - Get trade volume
- GET /api/trades/search - Search trades with filters

### ApiEndpointsTest
Basic validation tests for API endpoints:
- GET /api/quotes - Validate quotes endpoint responds
- GET /api/trades - Validate trades endpoint responds
- GET /api/quotes/count - Validate count endpoint
- GET /api/trades/count - Validate trade count endpoint
- POST /api/quotes/rfq - Validate RFQ endpoint

## Running Tests

**Important**: Tests require the FX API server to be running on http://localhost:8080

### Start the API Server

In one terminal:
```bash
cd fx-api
mvn spring-boot:run
```

### Run All Tests

In another terminal:
```bash
cd fx-api-test
mvn test -Dmaven.test.skip=false
```

### Run Specific Test Class

```bash
mvn test -Dmaven.test.skip=false -Dtest=QuoteApiContractTest
mvn test -Dmaven.test.skip=false -Dtest=TradeApiContractTest
mvn test -Dmaven.test.skip=false -Dtest=ApiEndpointsTest
```

### Custom API Base URL

To test against a different environment:
```bash
mvn test -Dmaven.test.skip=false -Dapi.base.url=http://your-server:8080
```

## Configuration

### System Properties

- `api.base.url`: Base URL of the API server (default: http://localhost:8080)
- `openapi.spec.path`: Path to the OpenAPI specification file

### Skip Tests by Default

Tests are skipped by default in the POM configuration because they require the API server to be running. To run tests, explicitly set `-DskipTests=false`.

## Test Technologies

- **REST Assured**: Fluent API for testing REST services
- **JUnit 5**: Testing framework
- **AssertJ**: Fluent assertions
- **Swagger Request Validator**: OpenAPI schema validation
- **JSON Path**: JSON query and validation

## Benefits

- **Contract Testing**: Ensures API implementation matches the specification
- **Regression Testing**: Catch breaking changes early
- **Documentation**: Tests serve as living documentation of API behavior
- **Validation**: Automated schema validation prevents contract violations
- **CI/CD Integration**: Can be integrated into continuous integration pipelines

## Example Output

```
[INFO] Running com.example.fx.test.QuoteApiContractTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.fx.test.TradeApiContractTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.fx.test.ApiEndpointsTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

## Best Practices

1. **Isolated Tests**: Each test should be independent and not rely on the state from other tests
2. **Test Data**: Tests create their own test data to ensure consistency
3. **Clean Assertions**: Use clear, meaningful assertions that describe expected behavior
4. **Error Cases**: Include tests for both success and error scenarios
5. **Performance**: Consider adding performance/load tests for critical endpoints
