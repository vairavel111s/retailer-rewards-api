# Retailer Rewards API

A Java 8 Spring Boot REST API that calculates customer reward points
for individual purchases and returns monthly and overall summaries
for a requested date range.

## Technology

- Java 8
- Spring Boot 2.7.18
- Maven
- JUnit 5
- Mockito
- Spring Boot Test and MockMvc

## Reward rules

Points are calculated separately for each transaction:

- The first $50 earns no points.
- Each whole dollar above $50 through $100 earns 1 point.
- Each whole dollar above $100 earns 2 points.

Examples:

| Purchase amount | Reward points |
|---|---:|
| $50 | 0 |
| $60 | 10 |
| $75 | 25 |
| $100 | 50 |
| $120 | 90 |
| $150 | 150 |

For a $120 purchase:

- $50 through $100: 50 points.
- $20 above $100: 40 points.
- Total: 90 points.

Two separate $60 purchases earn 10 + 10 = 20 points.
Purchase amounts are not combined before calculating points.

### Monetary assumptions

- Amounts are represented using BigDecimal.
- Only whole dollars in each purchase earn points.
- Fractional dollars are discarded for point calculation.
- Original amounts, including cents, are preserved in spending totals.
- For example, $120.75 earns 90 points.
- Negative and null purchase amounts are rejected.
- Point calculations that exceed the long range are rejected.

## Features

- Rewards for an individual customer.
- Rewards for all sample customers.
- Monthly and overall reward summaries.
- Transaction details with individual reward points.
- Dynamic date ranges with inclusive start and end dates.
- Zero-point summaries for months without purchases.
- Zero rewards for existing customers without purchases.
- Asynchronous transaction fetching.
- Input validation and structured error responses.
- Application logging.
- Unit and API integration tests.

## Design

The request flow is:

RewardController → RewardService → TransactionFetchService
→ TransactionRepository

RewardService also uses CustomerRepository to identify customers
and RewardCalculator to calculate points for each purchase.

### Responsibilities

| Component | Responsibility |
|---|---|
| RewardController | Exposes REST endpoints |
| RewardService | Validates requests and builds reward summaries |
| RewardCalculator | Calculates points for a single purchase |
| TransactionFetchService | Fetches transactions asynchronously |
| CustomerRepository | Provides sample customer data |
| TransactionRepository | Provides and filters sample transactions |
| GlobalExceptionHandler | Converts exceptions into HTTP error responses |
| DTO classes | Define API response structures |

### Data storage

The application uses in-memory sample data.

No database setup or external service credentials are required.
Data is reset when the application restarts.

### Asynchronous processing

TransactionFetchService uses Spring's @Async support with a named
ThreadPoolTaskExecutor.

This simulates asynchronous data retrieval using the local repository.
It does not call an external API or introduce artificial delays.

The executor uses:

- Core pool size: 2
- Maximum pool size: 4
- Queue capacity: 50
- Thread name prefix: transaction-fetch-

The controller returns CompletableFuture responses.

For the all-customers endpoint, customer reward futures are combined
using CompletableFuture.allOf. If one fetch fails, the overall request
fails rather than returning an incomplete result.

### Date handling

- Dates use ISO format: YYYY-MM-DD.
- Both startDate and endDate are required.
- The start date must not be after the end date.
- Both boundary dates are included.
- Only transactions within the requested range are counted.
- Monthly summaries use year and month, keeping different years separate.
- Every calendar month touched by the range is included.
- Partial-month ranges count only purchases within the requested dates.

## Project organization

Production code is under src/main/java/com/retailer/rewards.

| Package | Contents |
|---|---|
| config | Asynchronous executor configuration |
| controller | REST controller |
| dto | Customer, monthly, transaction and error responses |
| exception | Domain exceptions and global exception handling |
| model | Customer and Transaction models |
| repository | In-memory customer and transaction repositories |
| service | Calculation, asynchronous fetching and aggregation |

Tests are under src/test/java/com/retailer/rewards,
organized into controller, repository and service packages.

## Run locally

### Prerequisites

- JDK 8
- Maven

Confirm that Maven uses Java 8:

```bash
java -version
mvn -version
```

### IntelliJ IDEA

1. Open the project as a Maven project.
2. Set the project SDK and language level to Java 8.
3. Configure the Maven importer and runner to use JDK 8.
4. Reload the Maven project.
5. Run RewardsApplication.

### Command line

```bash
mvn spring-boot:run
```

The default base URL is:

```text
http://localhost:8080
```

## REST endpoints

### Rewards for one customer

```http
GET /api/customers/{customerId}/rewards?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
```

Example:

```text
http://localhost:8080/api/customers/101/rewards?startDate=2026-07-01&endDate=2026-09-30
```

The response contains:

- Customer ID and name.
- Requested start and end dates.
- Monthly transaction counts, spending and reward points.
- Individual transaction details and reward points.
- Overall spending and reward points.

### Rewards for all customers

```http
GET /api/rewards?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
```

Example:

```text
http://localhost:8080/api/rewards?startDate=2026-07-01&endDate=2026-09-30
```

Returns an array of customer reward responses, including customers
without purchases.

### Request parameters

| Parameter | Location | Requirement |
|---|---|---|
| customerId | Path, individual endpoint | Positive numeric customer ID |
| startDate | Query | Required ISO date |
| endDate | Query | Required ISO date, on or after startDate |

## Sample data and expected results

The main sample period is July through September 2026.

Additional transactions immediately outside this period are included
to verify date filtering.

For startDate=2026-07-01 and endDate=2026-09-30:

| Customer ID | Customer | July points | August points | September points | Total |
|---|---|---:|---:|---:|---:|
| 101 | Alice Morgan | 115 | 50 | 100 | 265 |
| 102 | Brian Wilson | 160 | 0 | 52 | 212 |
| 103 | Clara Evans | 0 | 0 | 0 | 0 |

Alice's spending total for this period is $525.75.

Clara is an existing customer without transactions.
An unknown customer is handled separately with HTTP 404.

## Error handling

Errors use a consistent JSON structure:

```json
{
  "timestamp": "2026-10-30T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with ID: 999",
  "path": "/api/customers/999/rewards"
}
```

The timestamp is generated when the error response is created.

| Condition | HTTP status |
|---|---:|
| Valid request | 200 |
| Missing required query parameter | 400 |
| Invalid date format or calendar date | 400 |
| Reversed date range | 400 |
| Non-numeric or non-positive customer ID | 400 |
| Unknown customer | 404 |
| Unexpected processing failure | 500 |

Asynchronous exception wrappers are unwrapped so that underlying
domain exceptions receive the appropriate response.

Unexpected failures are logged on the server. The API returns a
generic message without exposing internal exception details.

## Testing

Run the complete test suite:

```bash
mvn test
```

Alternatively, select Maven → Lifecycle → test in IntelliJ IDEA.

### Test results

Latest complete test run:

```text
Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test class | Tests |
|---|---:|
| RewardCalculatorTest | 17 |
| CustomerRepositoryTest | 4 |
| TransactionRepositoryTest | 10 |
| TransactionFetchServiceTest | 3 |
| RewardServiceTest | 15 |
| RewardApiIntegrationTest | 9 |
| Total | 58 |

### Covered scenarios

- Reward thresholds and fractional-dollar amounts.
- Separate calculation for individual purchases.
- Null, negative and overflowing calculation inputs.
- Customer lookup and unknown customers.
- Inclusive date boundaries and customer isolation.
- Empty transaction results.
- Asynchronous execution on a background thread.
- Repository failure propagation.
- Monthly and overall reward aggregation.
- Months and customers without purchases.
- Partial-month and cross-year ranges.
- Pending and failed asynchronous operations.
- All-customer aggregation and failure propagation.
- Successful API responses and JSON serialization.
- Invalid IDs, dates, ranges and missing parameters.
- HTTP 400 and 404 error responses.

API integration tests use the Spring application context and MockMvc.
They exercise asynchronous request dispatch without starting a
separate HTTP server.

Maven generates detailed test reports in target/surefire-reports.
Generated reports and build output are excluded from Git.

## Build

Create the application JAR:

```bash
mvn clean package
```

Run it:

```bash
java -jar target/retailer-rewards-api-1.0-SNAPSHOT.jar
```

## Scope

This project demonstrates reward calculation and retrieval using
sample data.

Database persistence, authentication, transaction creation and a
frontend are outside the current implementation.