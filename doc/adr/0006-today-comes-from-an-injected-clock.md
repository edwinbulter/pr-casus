# 0006. Today comes from an injected Clock

Date: 2026-10-25
Status: Accepted

## Context
The scenario "a three-kid family with only adult children does not match" makes the pattern depend on the date: at least one child must be under 18 **today**. The rules at the boundary must be testable exactly: a child who turns 18 today is no longer under 18, a child who turns 18 tomorrow still is.

Options:
1. **Call `LocalDate.now()` where the date is needed.** Simple, but the outcome of a test depends on the day it runs, and the boundary cannot be tested exactly.
2. **Inject a `java.time.Clock`.** Tests pass a fixed clock (`Clock.fixed(...)`); production uses the system clock.

## Decision
We choose **option 2**, because only a fixed clock makes the date rules exactly and reproducibly testable.

- `ClockConfig.clock()` is the only place that creates the system clock (the time zone is ADR 0007).
- `PeopleService` gets the `Clock` in its constructor and determines today in `register` with `LocalDate.now(clock)`.
- The domain rule `ThreeKidFamily.isMatchedBy(person, knownPerson, today)` gets today as a parameter; the domain knows a date, not a clock.
- The age is never stored. It is determined against the clock on every registration, so a match cannot go stale.

## Consequences
- `ThreeKidFamilyTests` and `PeopleServiceTests` use a fixed date (2026-10-25) and test both sides of the boundary.
- The acceptance scenarios run against the real system time. Their birth dates are chosen far from the age of 18, so they do not depend on the day they run.
- Any later caching of matches must leave the age check out, or invalidate it daily.

## Guard
ArchUnit `ArchitectureTests.onlyClockConfigReadsTheSystemTime`, in the commit stage. It reads the bytecode of all production classes and fails when a class other than `ClockConfig` makes a call that reads the system time:
- `now()` or `now(ZoneId)` of any `java.time` class (`LocalDate`, `LocalDateTime`, `ZonedDateTime`, `Instant`, …); only `now(Clock)` is allowed;
- a factory of the system clock: `Clock.systemDefaultZone()`, `Clock.systemUTC()` or `Clock.system(ZoneId)`;
- `System.currentTimeMillis()` or `new Date()`.

Seen red by sabotage in `PeopleService`, one at a time: `LocalDate.now()`, `LocalDate.now(ZoneId.of("UTC"))`, `ZonedDateTime.now(ZoneId.of("UTC"))`, `LocalDate.now(Clock.systemUTC())`, `System.currentTimeMillis()` and `new Date()`.

Not covered: reading the time through a library that calls the system clock itself.
