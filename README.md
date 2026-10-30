# Retailer Rewards API

A Spring Boot REST API project for calculating customer reward points
from purchase transactions.

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

For example, a $120 purchase earns:

(100 - 50) + (120 - 100) × 2 = 90 points.

## Assumptions

- Only whole dollars count toward rewards. Cents are discarded
  separately for each transaction.
- A $120.75 purchase earns 90 points.
- Zero-value purchases earn zero points.
- Null and negative transaction amounts are rejected.
- Calculations exceeding the long points range are rejected.
- Reward points are calculated per transaction before aggregation.
  Two separate $60 purchases earn 20 points in total.

## Current status

Completed:

- Java 8 and Maven project configuration.
- Spring Boot application startup.
- Transaction reward calculation.
- Reward calculator unit tests.

Not implemented yet:

- Sample customer and transaction dataset.
- Asynchronous transaction fetching.
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

REST endpoints are not available yet.

## Tests

Open the following test class in IntelliJ IDEA:

src/test/java/com/retailer/rewards/service/RewardCalculatorTest.java

Run the complete RewardCalculatorTest class.

### Verified results

17 test executions passed with no failures.

The tests cover:

- Amounts below, at, and above the $50 and $100 thresholds.
- Zero-value purchases.
- Decimal amounts using the whole-dollar assumption.
- Null and negative amounts.
- Independent calculation for separate purchases.
- Points exceeding the long range.