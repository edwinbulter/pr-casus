# Three-Kid Family Challenge

A Spring Boot service for the casus "[Three-Kid Family Challenge](doc/casus.md)", built with **acceptance test-driven development (ATDD)** as described by Dave Farley in *Modern Software Engineering* and *Continuous Delivery*.

> **Status:** step 0 (walking skeleton) is done: the service reports its build version, and the pipeline tests exactly the image of each commit. Phase 1: `POST /api/v1/people` answers **200** with everything known about the matching people when someone has a partner and exactly three children who all list that partner as parent, at least one of them under 18 (scenario 2); otherwise **444** without a body (scenarios 1, 3 and 5). People are kept in memory (ADR 0005); today comes from an injected clock (ADR 0006), as the date in Europe/Amsterdam (ADR 0007). Bidirectional integrity: parent and child relations are derived in both directions from all records (ADR 0008), and a matching person is returned with everything known, including derived relations (ADR 0009). Posting a known ID again replaces the old data, and can end a match (scenario 7). A registration only re-evaluates the family of the registered person (ADR 0010; see Performance). Phase 2 is done: `DELETE /api/v1/people` deletes IDs and answers **204**; a deleted ID no longer counts for matching, also where other records still name it, and a POST of a deleted ID is ignored for storage and matching (ADR 0011). What is left open, and why, is under [Open points](#open-points).

## What the service does

The service receives person records, one per request, and after **each** record decides immediately whether **any** stored person matches this pattern:

1. the person has a partner;
2. the person has **exactly 3 children**, and all 3 list that partner as mother or father;
3. **at least one** of those children is **under 18**.

### API

| Request | Body | Response |
| --- | --- | --- |
| `POST /api/v1/people` | One person: `id`, `name`, `birthDate`, `parent1`, `parent2`, `partner`, `children` (references as `{ "id": n }`) | **200 OK** with all people that currently match, with everything known about them. **444 No Response** if nobody matches yet |
| `DELETE /api/v1/people` (phase 2) | An array of IDs, e.g. `[17, 99, 2137]` | **204 No Content.** The IDs are deleted and ignored forever, for storage and for matching |

Rules from the casus:
- References may point to people who have never been posted.
- Posting the same ID again replaces the old data.
- Parent and child links are kept bidirectional: if A lists B as a child, B lists A as a parent (auto-repaired).
- All filtering happens in Java, not in SQL.

## Why ATDD

The casus is small, but it leaves questions open that the code has to answer one way or the other. For example:

- Marge names Bart as her child, but Bart has not been posted yet, so the service has only his ID. Does Bart count as one of Marge's three children?
- Marge names Homer as her partner, but Homer's own record names no partner. Does Homer have a partner?
- Bart's record names Homer and Marge as his parents. Later Bart is posted again with only Marge. Is Homer still his parent?

ATDD answers each such question with a concrete example, written as a test and approved before the code is built. Every evaluation criterion of the casus benefits:

- **Executable examples make the rules explicit.** Every behaviour starts as a scenario in domain language, with concrete values (Given, When, Then). Each interpretation of the casus is visible and reviewable in the scenarios, not hidden in the code.
- **Tests are a result of the design, not an afterthought.** Double-loop outside-in TDD: the outer loop is a failing acceptance scenario, the inner loop is red → green → refactor per class, from the controller inwards. The unit and integration tests the casus asks for come out of this.
- **Small commits by construction.** One iteration is one example, from scenario to green pipeline, in one small commit. Each commit message carries its evidence: which test was **seen red** and why, and what is **not verified**. The Git history tells how the solution grew.
- **The pipeline is the referee.** The commit stage runs the unit and integration tests, the architecture rules and the property tests. The acceptance stage starts the real Docker image and runs the scenarios against it. "It works" counts only when the tests show it.
- **The four-layer acceptance test model.** Scenarios (layer 1) use a domain-specific language (layer 2), which talks to the system through a protocol driver (layer 3), against the real running service (layer 4). The scenarios stay readable and stable while the implementation changes.
- **Decisions as ADRs, guarded by tests.** Architecture choices, such as the in-memory store, incremental matching and an injected clock, are recorded when a scenario asks for them. Each ADR names the fitness function that guards it, for example an ArchUnit rule.
- **Performance is designed for.** After each POST the service re-evaluates only the affected part of the family graph, not every stored person. A test that counts the work per registration and a load measurement back this up.

### AI as pair programmer within the loop

The way of working is captured as [Claude Code skills](.claude/skills/), copied unchanged from [banksim-mse](https://github.com/edwinbulter/banksim-mse), where they were developed:

- **[`atdd`](.claude/skills/atdd/README.md):** the procedure for one iteration, with stop points where the human approves the scenario and its expected values, each architecture decision and each commit. Hard rules: one failing scenario at a time, no production code without a failing test, refactoring only on green.
- **[`uitleg`](.claude/skills/uitleg/SKILL.md):** explains a part of the system (route through the code, design, critical points) to the developer who continues the work.

The project-specific part (commands, paths, conventions, pitfalls) is the ATDD profile in [`CLAUDE.md`](CLAUDE.md). Start an iteration with `/atdd next scenario`; ask for an explanation with `/uitleg <topic>`.

## Performance

After each registration the service only re-evaluates the family of the registered person: the person, its parents and the parents of its children ([ADR 0010](doc/adr/0010-incremental-matching.md)). Candidates are kept sorted by the day their youngest child turns 18, so who matches today is a range query against the clock. Relations are lookups by ID in reverse indexes ([ADR 0008](doc/adr/0008-relations-are-derived-in-both-directions.md)), and everything is in memory ([ADR 0005](doc/adr/0005-person-records-in-memory.md)).

Measured with `make perf`: in-process (the application service with the in-memory store, without HTTP), on a MacBook with Apple silicon. The population consists of families of five (two partners, three children); in ten families a child is under 18, so 20 people match.

Each row registers that many people one by one into an empty service. The total is the time for the whole run; the average is that total divided by the number of registrations.

| People registered | Before ADR 0010 (check everyone): total, average per registration | After ADR 0010 (incremental): total, average per registration |
| ---: | ---: | ---: |
| 10,000 | 24.8 s, 2,477 µs | 0.3 s, 29.5 µs |
| 20,000 | 99.4 s, 4,971 µs | 0.3 s, 15.3 µs |
| 100,000 | not measured (estimated ~40 minutes) | 1.4 s, 14.1 µs |

Before, the work of a registration grew with the number of people already stored: doubling the people doubled the average and quadrupled the total, and the last registrations of a run took about twice the average. After, the average stays the same however many people are stored (the first row includes the warm-up of the JVM). Two limits remain:
- The answer contains all matching people, as the casus asks, so building it grows with the number of people that match.
- Registrations are handled one at a time (`synchronized`), so throughput is bounded by a single thread; HTTP and JSON add their own cost per request.

The pipeline does not measure time, which is unreliable on shared CI runners. It guards the design instead: `PeopleServiceWorkTests` counts the candidates evaluated per registration, and the property test `IncrementalMatchingProperties` checks that the incremental answer equals checking everyone.

## Tech stack

Java 21, Spring Boot 4, Maven, Jib (Docker image), JUnit, jqwik, ArchUnit, Testcontainers and GitHub Actions.

## Getting started

Requirements: Java 21 and Docker (or OrbStack). Maven comes with the wrapper.

```bash
make commit-stage   # unit and integration tests
make acceptance     # build the image three-kids:<version> with Jib and run the acceptance scenarios against it
make run            # run the service on http://localhost:8080
make perf           # measure the time per registration (not part of the pipeline)
make help           # all targets
```

## Try it

1. Start the service, fresh: people are kept in memory, so a restart begins empty.
   ```bash
   make run                              # with Docker: builds and runs the image
   ./mvnw -pl service spring-boot:run    # without Docker
   ```
2. Open [`doc/requests.http`](doc/requests.http) in IntelliJ IDEA (click ▶ next to a request) or in VS Code with the REST Client extension.
3. Run the requests from top to bottom. Each request says which answer to expect: the Simpsons are built up step by step (444, then 200 with Marge, then 200 with Homer and Marge), Maggie's birth date is updated, Maggie is deleted (204), and posting her again is ignored.

## Open points

The casus asks for phase 1, and phase 2 if there is time; both are done. The points below are deliberately left open. Each one is outside what the casus asks, or would cost more time than it adds to this submission. The backlog in the [plan](doc/initial-plan.md) has the status of every item.

| Open point | What the service does today | Why it is left open |
| --- | --- | --- |
| **Is a partner link symmetric?** (backlog item 8) | A person's partner is what its own record says. A person known only by ID, because others name it, cannot match until its own record is posted. | The casus does not say whether "has a partner" may come from the other person's record. Assuming it would add behaviour the casus does not ask for. Posting each person's record, as in the casus example, makes the match appear. |
| **Conflicting parents** (backlog item 9) | Parents are the union of both directions (ADR 0008). When a third person names a child that already names two parents, the child has three parents. | The casus does not describe this case, and deciding what wins (for example the child's own record) is a domain decision, not a technical one. It needs its own scenario and an ADR that replaces ADR 0008. |
| **Four children** (backlog item 4) | "Exactly 3" is in the rule; two children are tested by a scenario and a unit test. | An acceptance scenario with four children would be green at once. It was skipped in favour of a scenario that drove new behaviour (another second parent). |
| **Mutation testing (PIT)** | Each iteration was sabotaged by hand to see a test fail; the commit messages record it under **Sabotage**. | Adding PIT to the pipeline takes an ADR and tuning of a threshold. Hand-made sabotage is a one-off check, so PIT is the first fitness function to add next. |
| **A concurrency test** | Registrations and deletions are `synchronized` on the service (see Performance). | One lock makes the writes sequential, so there is no interleaving to test yet. The test becomes worthwhile once the writes are made parallel. |
| **An ArchUnit rule for a domain without Spring** | The domain package has no Spring dependency. | It is true today, but not yet guarded; the only ArchUnit rule guards the clock (ADR 0006). |

## Documentation

| Document | Content |
| --- | --- |
| [Casus](doc/casus.md) | The assignment: pattern, rules, evaluation criteria, phase 1 and 2 |
| [Technisch ontwerp](doc/technisch-ontwerp.md) | Technical design for newcomers: architecture, core mechanisms, tests, pipeline, decisions and open points (in Dutch) |
| [Example requests](doc/requests.http) | Requests to try the API by hand, with the expected answer per request |
| [Initial plan](doc/initial-plan.md) | Steps, scenario backlog, domain ambiguities to decide, design and performance considerations, time box |
| [`doc/adr/`](doc/adr) | Architecture Decision Records, written during the build |
| [`CLAUDE.md`](CLAUDE.md) | The ATDD profile of this project |
| [Skill `atdd`](.claude/skills/atdd/README.md) | Use and value of the ATDD skill (in Dutch) |

## Time spent

5 hours.
