# Three-Kid Family Challenge

A Spring Boot service for the casus "[Three-Kid Family Challenge](doc/casus.md)", built with **acceptance test-driven development (ATDD)** as described by Dave Farley in *Modern Software Engineering* and *Continuous Delivery*.

> **Status:** step 0 (walking skeleton) is done: the service reports its build version, and the pipeline tests exactly the image of each commit. Phase 1: `POST /api/v1/people` answers **444** without a body when nobody matches (scenario 1), and **200** with everything known about the matching people when someone has a partner and exactly three children (scenario 2). People are kept in memory (ADR 0005). Not yet checked: that the children name the partner as parent, and that one of them is under 18 (scenarios 3 and 5 in the [plan](doc/initial-plan.md)).

## What the service does

The service receives person records, one per request, and after **each** record decides immediately whether **any** stored person matches this pattern:

1. the person has a partner;
2. the person has **exactly 3 children**, and all 3 list that partner as mother or father;
3. **at least one** of those children is **under 18**.

### API

| Request | Body | Response |
| --- | --- | --- |
| `POST /api/v1/people` | One person: `id`, `name`, `birthDate`, `parent1`, `parent2`, `partner`, `children` (references as `{ "id": n }`) | **200 OK** with all people that currently match, with everything known about them. **444 No Response** if nobody matches yet |
| `DELETE /api/v1/people` (phase 2) | An array of IDs, e.g. `[17, 99, 2137]` | The IDs are deleted and ignored forever, for storage and for matching |

Rules from the casus:
- References may point to people who have never been posted.
- Posting the same ID again replaces the old data.
- Parent and child links are kept bidirectional: if A lists B as a child, B lists A as a parent (auto-repaired).
- All filtering happens in Java, not in SQL.

## Why ATDD

The casus is small, but its rules leave room for interpretation. Does a child known only by ID count? Is a partner link symmetric? What does an update do to links that were inferred earlier? ATDD turns each interpretation into an explicit, executable example, and every evaluation criterion of the casus benefits:

- **Executable examples make the rules explicit.** Every behaviour starts as a scenario in domain language, with concrete values (Given, When, Then). Each interpretation of the casus is visible and reviewable in the scenarios, not hidden in the code.
- **Tests are a result of the design, not an afterthought.** Double-loop outside-in TDD: the outer loop is a failing acceptance scenario, the inner loop is red → green → refactor per class, from the controller inwards. The unit and integration tests the casus asks for come out of this.
- **Small commits by construction.** One iteration is one example, from scenario to green pipeline, in one small commit. Each commit message carries its evidence: which test was **seen red** and why, and what is **not verified**. The Git history tells how the solution grew.
- **The pipeline is the referee.** The commit stage runs the unit and integration tests, architecture rules and mutation testing. The acceptance stage starts the real Docker image and runs the scenarios against it. "It works" counts only when the tests show it.
- **The four-layer acceptance test model.** Scenarios (layer 1) use a domain-specific language (layer 2), which talks to the system through a protocol driver (layer 3), against the real running service (layer 4). The scenarios stay readable and stable while the implementation changes.
- **Decisions as ADRs, guarded by tests.** Architecture choices, such as the in-memory store, incremental matching and an injected clock, are recorded when a scenario asks for them. Each ADR names the fitness function that guards it, for example an ArchUnit rule.
- **Performance is designed for.** After each POST the service re-evaluates only the affected part of the family graph, not every stored person. Concurrency tests and a simple load measurement back this up.

### AI as pair programmer within the loop

The way of working is captured as [Claude Code skills](.claude/skills/), copied unchanged from [banksim-mse](https://github.com/edwinbulter/banksim-mse), where they were developed:

- **[`atdd`](.claude/skills/atdd/README.md):** the procedure for one iteration, with stop points where the human approves the scenario and its expected values, each architecture decision and each commit. Hard rules: one failing scenario at a time, no production code without a failing test, refactoring only on green.
- **[`uitleg`](.claude/skills/uitleg/SKILL.md):** explains a part of the system (route through the code, design, critical points) to the developer who continues the work.

The project-specific part (commands, paths, conventions, pitfalls) is the ATDD profile in [`CLAUDE.md`](CLAUDE.md). Start an iteration with `/atdd next scenario`; ask for an explanation with `/uitleg <topic>`.

## Tech stack

Java 21, Spring Boot 4, Maven, Jib (Docker image), JUnit 5, jqwik, ArchUnit, PIT, Testcontainers and GitHub Actions.

## Getting started

Requirements: Java 21 and Docker (or OrbStack). Maven comes with the wrapper.

```bash
make commit-stage   # unit and integration tests
make acceptance     # build the image three-kids:<version> with Jib and run the acceptance scenarios against it
make run            # run the service on http://localhost:8080
make help           # all targets
```

## Documentation

| Document | Content |
| --- | --- |
| [Casus](doc/casus.md) | The assignment: pattern, rules, evaluation criteria, phase 1 and 2 |
| [Initial plan](doc/initial-plan.md) | Steps, scenario backlog, domain ambiguities to decide, design and performance considerations, time box |
| [`doc/adr/`](doc/adr) | Architecture Decision Records, written during the build |
| [`CLAUDE.md`](CLAUDE.md) | The ATDD profile of this project |
| [Skill `atdd`](.claude/skills/atdd/README.md) | Use and value of the ATDD skill (in Dutch) |

## Time spent

To be filled in on submission.
