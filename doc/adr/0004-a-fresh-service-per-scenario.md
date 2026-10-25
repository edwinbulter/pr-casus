# 0004. Every acceptance scenario runs against a fresh service

Date: 2026-10-25
Status: Accepted

## Context
The answer to every registration depends on **all** people stored in the service: "does anyone match?". Aliases with unique IDs per scenario therefore do not isolate scenarios. After a scenario that registers a complete three-kid family, every later scenario on the same service would get 200 instead of 444. The scenario "a person without a three-kid family does not match" forces the choice.

Options:
1. **A fresh container per scenario.** Starting the image takes about 2 to 3 seconds.
2. **One container, scenarios in a fixed order.** Fast, but scenarios depend on each other and a new scenario can break an old one.
3. **A reset endpoint.** Fast, but it is test-only code in the service, which the acceptance test model forbids.

## Decision
Every scenario gets its own container of the image under test. The DSL objects (`Registry`, `Operator`) start it through the driver and stop it when the scenario ends (JUnit `@AutoClose`).

## Consequences
- Scenarios are independent and can run in any order.
- Each scenario costs a container start: roughly 2 to 3 seconds, so the acceptance stage grows linearly with the number of scenarios.
- The service itself does not change; the acceptance scenarios say nothing about performance with many stored people. That needs a separate performance test.

## Guard
- `MatchingScenarios` expects 444 for a person without a three-kid family; on a shared service with a matching family it would get 200.
- The driver only obtains a service through `HttpServiceDriver.onAFreshService()`; there is no shared container.
