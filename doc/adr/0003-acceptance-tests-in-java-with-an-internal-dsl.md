# 0003. Acceptance tests in Java with an internal DSL and an HTTP driver

Date: 2026-10-25
Status: Accepted

## Context
The rules of the casus are ambiguous; every interpretation must become an executable example in domain language. The service only has a REST API, no UI. banksim-mse uses Playwright TS for its acceptance tests because it has a browser UI; here that would add Node to a Java project without a benefit.

## Decision
- The acceptance tests are a separate Maven module `acceptance`, in Java with JUnit 5 and AssertJ, in the four layers of the acceptance test model:
  1. scenarios (`scenarios/`, classes `*Scenarios`): Given, When, Then in domain language, without URLs or JSON;
  2. DSL (`dsl/`): domain methods and the assertions;
  3. protocol driver (`drivers/`): the interface `ServiceDriver` and the implementation `drivers/http/HttpServiceDriver` with the JDK `HttpClient`, without assertions;
  4. system: the image of the current build, started with Testcontainers.
- The module compiles in the commit stage and only runs its scenarios with `-DskipITs=false` (`make acceptance`, the acceptance stage).

## Consequences
- One language and one build; the DSL can later get a second driver (for example directly against the application service) without changing the scenarios.
- The scenarios need Docker.
- New dependencies: Testcontainers (managed by Spring Boot) for the system layer; Jackson for reading JSON in the driver.

## Guard
- The scenarios contain no URLs, JSON or HTTP status codes; the DSL and the driver hold those. Checked in review; an ArchUnit rule (scenarios do not depend on `drivers`) follows when there are more scenarios.
- `make commit-stage` compiles the acceptance module, so a broken DSL fails the commit stage.
