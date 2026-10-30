# Retailer Rewards API

A Spring Boot project for calculating customer reward points from
purchase transactions.

## Technology

- Java 8
- Spring Boot 2.7.18
- Maven
- JUnit 5

Spring Boot 2.7.18 is used to meet the assignment's Java 8 requirement.

## Reward rules

Points are calculated independently for each transaction:

- No points for the first $50.
- One point per whole dollar spent above $50 and up to $100.
- Two points per whole dollar spent above $100.

A $120 purchase earns 50 + (20 x 2) = 90 points.

## Assumptions

- Only whole dollars count toward rewards. Cents are discarded
  separately for each transaction.
- A $120.75 purchase earns 90 points.
- Zero-value purchases earn zero points.
- Null and negative amounts are rejected.
- Calculations exceeding the long points range are rejected.
- Two separate $60 purchases earn 20 points in total.
- Date filtering includes both the start date and end date.
- Dates represent purchase calendar dates without a time component.
- Sample data is stored in memory; no database is required.

## Design

- model: Immutable customer and transaction objects.
- repository: Sample data access and inclusive date filtering.
- service: Transaction reward calculation.

Repositories expose customer lookup and transaction filtering.
An unknown customer returns an empty Optional from customer lookup.
A transaction query without matching purchases returns an empty list.

## Sample dataset

The dataset contains three customers and eleven transactions.
Nine transactions fall within July through September 2026.
Two additional transactions fall immediately outside that period.

Expected rewards for 2026-07-01 through 2026-09-30:

| Customer ID | Customer | July | August | September | Total |
|---|---|---:|---:|---:|---:|
| 101 | Alice Morgan | 115 | 50 | 100 | 265 |
| 102 | Brian Wilson | 160 | 0 | 52 | 212 |
| 103 | Clara Evans | 0 | 0 | 0 | 0 |

These are reference totals for the upcoming aggregation implementation.

Alice has purchases on both date boundaries.
Brian has no August purchases.
Clara has no transactions.
Alice's June 30 and October 1 purchases are outside this date range.

## Current status

Completed:

- Java 8 and Maven configuration.
- Spring Boot application startup.
- Transaction reward calculation.
- Customer and transaction models.
- Sample customer and transaction repositories.
- Inclusive transaction date filtering.
- Calculator and repository unit tests.
- Asynchronous transaction fetching.

Remaining:

- Monthly and overall reward aggregation.
- REST endpoints.
- API validation and error responses.
- Service and API integration tests.

## Run locally

1. Open the project in IntelliJ IDEA.
2. Configure JDK 8 as the project SDK.
3. Configure JDK 8 for the Maven importer and runner.
4. Reload the Maven project.
5. Run com.retailer.rewards.RewardsApplication.

The application starts on port 8080 by default.
REST endpoints are not implemented yet.

## Run tests

In IntelliJ IDEA, open the Maven tool window and run Lifecycle > test.

Alternatively, with Maven installed and JAVA_HOME pointing to JDK 8:

    mvn test

## Previous verification 

Maven test run completed successfully:

    Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
    BUILD SUCCESS

| Test class | Executions |
|---|---:|
| RewardCalculatorTest | 17 |
| TransactionRepositoryTest | 10 |
| CustomerRepositoryTest | 4 |

Tests cover reward thresholds, decimal amounts, invalid calculator
inputs, independent purchases, points overflow, customer lookup,
dataset protection, customer isolation, inclusive date boundaries,
empty transaction results, and invalid query arguments.

Maven generates detailed reports in target/surefire-reports.
Generated reports and build output are excluded from Git.


## Asynchronous fetching update

TransactionFetchService simulates asynchronous data retrieval from
the in-memory transaction repository.

Spring invokes the service through an async proxy and executes the
fetch on the transactionExecutor thread pool.

Executor settings:

- Core pool size: 2
- Maximum pool size: 4
- Queue capacity: 50
- Thread name prefix: transaction-fetch-
- Shutdown wait limit: 10 seconds

The service returns a CompletableFuture containing the transactions.
Repository failures complete the future exceptionally.
No external HTTP request or artificial delay is used.

### Latest verification

    Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
    BUILD SUCCESS

TransactionFetchServiceTest adds three checks:

- Fetching executes on a background thread.
- No matching transactions produce an empty list.
- Repository failures propagate through the future.

Asynchronous fetching is complete. Monthly aggregation, REST
endpoints, API error handling, and their tests remain to be implemented.