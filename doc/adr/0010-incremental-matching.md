# 0010. A registration only re-evaluates the family of the registered person

Date: 2026-10-25
Status: Accepted

## Context
The casus is evaluated on "what your application would do under load". Until now every registration checked all stored people against the pattern: O(n) work per registration and O(n²) for n registrations. Measured in-process: registering 10,000 people took 24.8 s in total (2.5 ms per registration on average), and registering 20,000 people took 99.4 s (5.0 ms on average). These are averages over the whole run: the work per registration grows with the number of people already stored, so the last registrations of a run take about twice the average.

Options:
1. **Check everyone** on every registration (as before): simple, but linear per registration.
2. **Incremental:** keep the structural candidates and only re-evaluate the people whose match can change.
3. **Cache the whole answer:** the age changes with time (ADR 0006), so the cache needs daily invalidation, and a registration still has to find out whose match changed.

## Decision
We choose **option 2** (chosen by the user).

- A person's match can only change when its own record changes, when the records of its children change, or when someone names it as parent or child. So `PeopleService.register` re-evaluates the registered person, its parents, and the parents of its children, both before and after saving. Relations are looked up by ID through the reverse indexes (ADR 0008).
- `ThreeKidFamily.minorUntil(person, relations)` gives, for a structural candidate (a partner and three known children who all have that partner as parent), the day its youngest child with a known birth date turns 18.
- `PeopleService` keeps the candidates sorted by that day. Who matches today is a range query, compared with the clock on every answer: the age is never stored as an outcome (ADR 0006).
- `register` is `synchronized`: registrations are handled one at a time, so the store and the candidates always change together.

## Consequences
- Measured in-process with `make perf`: about 15 µs per registration on average, both when registering 20,000 and 100,000 people (see the README). The time of a registration no longer depends on how many people are already stored.
- The answer must contain all matching people (casus), so its size, and the time to build it, grows with the number of people that match. Matching is rare by nature of the pattern.
- Registrations are serialized: throughput is bounded by one thread, at roughly 15 µs per registration in-process. HTTP and JSON add their own cost per request.
- `PeopleService` now keeps state next to the store; both are in memory (ADR 0005) and lost on a restart.

## Guard
- `PeopleServiceWorkTests`: with 1,000 people stored, one registration evaluates at most 10 candidates (counted via `childrenOf`). It was red at 1,001 with option 1.
- `IncrementalMatchingProperties` (jqwik, 5,000 random sequences of registrations, including updates and unknown references): after every registration, the answer equals checking all stored people. It caught the sabotages: without the parents, without the parents of the children, only after saving, and without removing an old candidate.
- `PeopleServiceTests.aFamilyWhoseYoungestChildTurns18TodayNoLongerMatches`: caught the sabotage that counts the day of the 18th birthday as under 18.
- The acceptance scenarios, among which the update in `UpdateScenarios`.
