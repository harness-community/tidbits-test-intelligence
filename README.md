# E-Commerce Application — Harness Test Intelligence Demo

A Java Spring Boot e-commerce platform with a comprehensive unit test suite
designed to demonstrate **Harness Test Intelligence (TI)** in a CI pipeline.

---

## Project Structure

```
src/
├── main/java/com/example/ecommerce/
│   ├── model/
│   │   ├── Product.java         — Product entity with stock management
│   │   ├── Customer.java        — Customer with loyalty tiers
│   │   ├── Order.java           — Order with lifecycle state machine
│   │   ├── OrderItem.java       — Line item with discount logic
│   │   ├── Coupon.java          — Coupon with validation & calculation
│   │   └── Address.java         — Embeddable address
│   ├── repository/              — Spring Data JPA repositories
│   ├── service/
│   │   ├── ProductService.java  — Inventory & search logic
│   │   ├── CustomerService.java — Registration, tiers, deactivation
│   │   └── OrderService.java    — Full order lifecycle + pricing
│   ├── util/
│   │   ├── PricingCalculator.java      — Tax, shipping, tier discounts
│   │   └── OrderNumberGenerator.java   — Sequential order numbers
│   └── exception/               — Custom exceptions
└── test/java/com/example/ecommerce/
    ├── model/                   — Unit tests for all model logic
    ├── service/                 — Mockito-based service tests
    └── util/                    — Utility class tests
```

---

## Test Coverage (~200+ tests)

| Test Class                 | Tests | What it covers                                |
|----------------------------|-------|-----------------------------------------------|
| `ProductTest`              | ~20   | Stock management, builder, validation          |
| `OrderItemTest`            | ~15   | Line totals, discount calculations             |
| `CouponTest`               | ~25   | Validity, discount types, caps, usage limits   |
| `AddressTest`              | ~12   | Formatting, US detection                       |
| `OrderTest`                | ~15   | Item management, cancellation/shipping logic   |
| `PricingCalculatorTest`    | ~30   | Tax by country, shipping tiers, tier discounts |
| `OrderNumberGeneratorTest` | ~15   | Format validation, uniqueness, date extraction |
| `ProductServiceTest`       | ~30   | CRUD, search, stock update, deactivation       |
| `CustomerServiceTest`      | ~25   | Registration, tier upgrades, deactivation      |
| `OrderServiceTest`         | ~30   | Order creation, cancellation, lifecycle        |

---

## Running the Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ProductServiceTest

# Run a specific test method
mvn test -Dtest=PricingCalculatorTest#calculateTax_forKnownCountries
```

---

## How to Use with Harness Test Intelligence

1. Import the `.harness/pipeline.yaml` into your Harness project
2. Connect your GitHub repo
3. Push a code change — e.g., modify `PricingCalculator.java`
4. Harness TI will **only run** `PricingCalculatorTest` (and related tests)
   instead of the full ~200-test suite
5. Compare run time vs. full suite to see the savings

### Example TI Savings

| Change Made                     | Full Suite | With TI        |
|---------------------------------|------------|----------------|
| Edit `PricingCalculator.java`   | ~45s       | ~8s (1 class)  |
| Edit `Coupon.java`              | ~45s       | ~10s (2 classes)|
| Edit `OrderService.java`        | ~45s       | ~15s (3 classes)|
| Edit `README.md` (no .java)     | ~45s       | ~2s (0 tests)  |

---

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **JUnit 5** + **Mockito** + **AssertJ**
- **H2** in-memory database (tests)
- **Maven** build tool
- **Harness CI** with Test Intelligence
