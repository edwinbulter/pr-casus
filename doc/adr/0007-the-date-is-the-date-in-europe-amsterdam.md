# 0007. The date is the date in Europe/Amsterdam

Date: 2026-10-25
Status: Accepted

## Context
With an injected clock (ADR 0006), the system clock still needs a time zone to turn the current moment into a date. Between midnight and 01:00 (winter) or 02:00 (summer) Dutch time, it is already the next day in the Netherlands but not yet in UTC. A child who turns 18 on that day is no longer under 18 in Dutch time, but still is in UTC during that hour.

Options:
1. **The system default time zone.** No configuration, but the outcome depends on where the service runs; in the distroless image it is UTC.
2. **A fixed zone: Europe/Amsterdam.** The date of the people in the register, wherever the service runs.

## Decision
We choose **option 2**, because the service serves Dutch local government: an age is determined on the date in the Netherlands, not on the date of the server.

- `ClockConfig.clock()` returns `Clock.system(ZoneId.of("Europe/Amsterdam"))`.

## Consequences
- The outcome does not depend on the time zone of the host or the container.
- Birth dates are plain dates without a zone; only "today" depends on the zone.
- A service for another country would need another zone; then it becomes configuration.

## Guard
- The ArchUnit rule of ADR 0006 keeps `ClockConfig` the only place that creates the system clock, so the zone is set in one place.
- The zone itself has no automated test: `ClockConfig` only runs when the Spring context starts. It is checked in review.
