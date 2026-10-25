# Initial plan – Three-Kid Family Challenge with ATDD

> Status: approved initial plan. Progress is tracked in the README, the scenarios and `doc/adr/`.

## Context

[`doc/casus.md`](casus.md) describes the casus "Three-Kid Family Challenge":

- A REST service receives `POST /api/v1/people`, one person record per request. A record holds `id`, `name`, `birthDate`, `parent1`, `parent2`, `partner` and `children`, each referenced by `{ "id": n }`. Referenced IDs may be unknown. Posting the same ID again replaces the old data.
- After each POST the service checks whether any stored person matches the pattern:
  1. has a partner;
  2. has exactly 3 children, all of whom list that partner as mother or father;
  3. at least one of those children is under 18.
- If someone matches, the service answers **200** with all matching people, including everything known about them. Otherwise it answers **444**.
- Rules:
  - Java 21+ with Spring Boot;
  - all filtering in Java, with no SQL `WHERE`;
  - bidirectional integrity between parent and child, enforced or auto-repaired;
  - unit and integration tests.
- Phase 2, only if there is time: `DELETE /api/v1/people` with an ID array. The service deletes those IDs and ignores them forever, for both storage and matching.
- Evaluation covers clean code (architecture, tests, docs), Git usage (small commits) and performance under load. The estimate is 2 hours, and the time spent must be disclosed.

We solve it with Dave Farley's ATDD way of working, as in [edwinbulter/banksim-mse](https://github.com/edwinbulter/banksim-mse). That project's skills `atdd` and `uitleg` (`~/IdeaProjects/banksim-mse/.claude/skills/`) are reused here.

**Decisions taken:**
- Everything is in English: code, scenarios, ADRs, README and commits.
- Acceptance tests are written in Java/JUnit 5, using the four-layer model with an internal DSL and an HTTP driver.
- Pipeline: GitHub Actions plus a Docker image built with Jib. There is no Kubernetes.

## Step A – Repository setup (no production code yet)

1. **Git.** Run `git init` with branch `main`. Add a `.gitignore` for Maven, IntelliJ and macOS, and an `.editorconfig` copied from banksim-mse.
2. **Plan.** Copy this plan to `doc/initial-plan.md`.
3. **Skills.** Copy `banksim-mse/.claude/skills/atdd` and `.../uitleg` to `.claude/skills/`, **unchanged**. They are designed to be generic, and translating them would make them drift from banksim-mse. Their language stays Dutch; the project profile says "Language: English".
   - Keep all references, including `stack-playwright-ts`, `stack-angular-vitest` and `stack-helm-bats`, so the skill stays an identical copy. Only `stack-java-spring`, `walking-skeleton`, `adr-sjabloon` and `profiel-sjabloon` are relevant here.
4. **`CLAUDE.md`** gets a `## ATDD-profiel` section, filled in from `references/profiel-sjabloon.md`. Mark every command as "not yet verified" until it has run.
   - **Stack:** Java 21, Spring Boot 4 (Maven wrapper, Jib), JUnit 5, jqwik, ArchUnit, PIT, Testcontainers and GitHub Actions. New tools only with an ADR.
   - **Commands:** `make commit-stage` (`mvnw verify` + PIT), `make image` (Jib to the local Docker daemon), `make acceptance` (the acceptance module against the image), and one scenario via `./mvnw -pl acceptance verify -Dit.test=<Class>`.
   - **Paths:** see the table in step B.
   - **Target environment:** a local Docker container and the CI runner.
   - **Conventions:**
     - commit message headings **Seen red**, **Sabotage** and **Not verified**;
     - never commit or push without permission;
     - one ATDD step per small commit.
   - **Backlog:** `doc/initial-plan.md`, section "Scenario backlog".
5. **`README.md`**, in English:
   - **What it is:** the casus in short, the API (POST/DELETE, 200/444), and how to build, run and test.
   - **Why ATDD (Farley):**
     - Executable examples make the ambiguous rules of the casus explicit; every interpretation is a reviewable scenario.
     - Double-loop outside-in TDD yields unit and integration tests as a by-product of design, not afterwards.
     - Every iteration is one small, green commit, which matches the "small commits" evaluation. The commit messages carry the evidence (seen red, not verified).
     - The deployment pipeline is the referee: commit stage and acceptance stage against the real container.
     - Architecture decisions are ADRs with a fitness function, for example the in-memory store, the clock and incremental matching.
     - AI acts as pair programmer inside the loop, steered by the `atdd` skill, with a human approving scenarios and decisions.
   - **Working with Claude Code:** `/atdd next scenario` and `/uitleg <topic>`.
   - **Time spent:** filled in when the work is submitted.
   - **Links:** `doc/initial-plan.md`, `doc/adr/` and banksim-mse.
6. ⛔ **Public GitHub repo** (`gh repo create`): only after the user confirms.
   - ⛔ Open question: a commit prefix (banksim-mse uses `[ECS-63]`). Default: no prefix.

Commit 1 is "Project setup: plan, README, ATDD profile and skills".

## Step B – Walking skeleton and pipeline (step 0 of the `atdd` skill)

**Module layout**, as a Maven multi-module build:

| Change | Where | Test |
| --- | --- | --- |
| Scenario (layer 1) | `acceptance/src/test/java/.../scenarios/` | JUnit 5 against the container |
| DSL (layer 2) | `acceptance/src/test/java/.../dsl/` | via the scenarios |
| Protocol driver (layer 3) | `acceptance/src/test/java/.../drivers/http/` (`RestClient`/JDK `HttpClient`) | via the scenarios |
| Domain (family graph, pattern) | `service/src/main/java/.../domain/` (no Spring) | JUnit, jqwik |
| API and application | `service/src/main/java/.../api`, `.../application` | `@WebMvcTest`, `@SpringBootTest` + MockMvc (integration) |
| Architecture rules | `service/src/test/java/.../architecture/` | ArchUnit |
| ADR | `doc/adr/NNNN-title.md` | – |

**Skeleton scenario:** "the service is running and reports its version". The DSL starts the Jib image with Testcontainers `GenericContainer` and asks for the version (actuator `info` with the git commit). Then it asserts that the version is the commit that was just built, so the test proves it ran against the code you just built.

**Pipeline** (`.github/workflows/ci.yml`):
- **Commit stage:** `mvnw verify` and PIT, which also builds the image once.
- **Acceptance stage:** starts only after a green commit stage. It runs the acceptance module against exactly that image.
- See the pipeline **red once**, with the acceptance stage not running when the commit stage fails, and then green.

**First ADRs** (template `adr-sjabloon.md`; ⛔ the user accepts):
- 0001 record architecture decisions;
- 0002 the deployment pipeline: Jib image, and Testcontainers as the acceptance environment;
- 0003 acceptance tests in Java with an internal DSL.

## Step C – Phase 1 scenarios (one ATDD iteration per scenario = one small commit)

Each scenario goes through the `atdd` iteration:

1. ⛔ scenario and expected values;
2. seen red for the right reason;
3. DSL/driver;
4. outside-in inner loop (controller → application service → domain → store);
5. ⛔ ADR if needed;
6. commit stage and acceptance stage local;
7. ⛔ commit;
8. report.

### Scenario backlog, in order

1. A single unknown person is posted → **444**. This scenario introduces the 444 status, using `ResponseEntity.status(444)` because 444 is not in `HttpStatus`, plus a body decision (⛔).
2. A complete family is built through successive POSTs: a parent with a partner and 3 children, both parents on every child, one child under 18 → the last POST gets **200**, and the body contains the matching person(s).
3. The same family, but all children are 18 or older → **444**.
4. 2 or 4 children → **444**, for "exactly 3".
5. One child has a different second parent → **444**.
6. **Bidirectional integrity:** the parent lists the child, and the child record does not name the parent, or the reverse. Auto-repair makes the match work anyway.
7. **Update:** reposting an ID replaces the old data. A match can disappear through this, and stale inferred links are removed.
8. **Response content:** a matching person who is only known by ID (no name or birth date yet) is still returned with everything that is known.
9. **Conflicting parents** (added during scenario 6a, ADR 0008): a child whose own record names two parents is also named as a child by a third person. Today the third person also counts as a parent, because the parents are the union of both directions. ⛔ Decide what wins (for example: the child's own record), then build it as its own scenario.
10. **Phase 2, only if time is left:** `DELETE` with an ID array removes those people. A later POST with such an ID is ignored silently, and references to ignored IDs do not count for matching. Check that the driver sends a JSON body with DELETE. Split into two iterations:
    - **10a. Deleting, and references to deleted IDs** (done, ADR 0011): `DELETE` answers 204; a deleted ID no longer counts as child, parent or partner, also where other records still name it, and is left out of responses. Scenario `DeletionScenarios.aDeletedPersonNoLongerCountsForMatching`.
    - **10b. A deleted ID that is posted again is ignored** ("for both storage and matching") (done, ADR 0011):
      - *Matching* already ignores it since 10a: the store leaves deleted IDs out of every relation, so such a POST changes no answer. An acceptance scenario for it would be green at once; it is a safety net, seen red only by sabotage.
      - *Storage* does not ignore it yet: `InMemoryPeople.save` stores the record again, so `find` and `all()` return it. This is not visible through the API, so it is driven by a unit test on `InMemoryPeople` ("saving a deleted ID stores nothing"), which is red now.

### Domain ambiguities: ⛔ the user approves each one as a scenario before it is built

These are proposed defaults, not decisions:
- **Children** are the union of the person's own `children` and the people who name this person as `parent1`/`parent2`, after bidirectional repair.
- **Partner:** is the link symmetric? If A names B, is B's partner A? Proposed: yes, auto-repaired.
- **Child known only by ID:** it counts toward "exactly 3" when its parents are known through the parents' records.
- **Missing birth date:** a child without a birth date does **not** count as under 18.
- **"All matching people":** only the people who match the pattern, not their partners or children.
- **Ignored IDs (phase 2):** they are also removed from references in other records.

## Design and performance (as ADR candidates, decided when a scenario asks for them)

- **In-memory store:** a `ConcurrentHashMap<Long, Person>` plus an index of reverse links. It is fast, there is no SQL at all, and so the "all filtering in Java" rule holds. Writes are serialized (a lock per POST, or a single writer) to keep the graph consistent; reads are lock-free.
- **Incremental matching:** a POST does not rescan everything. The service re-evaluates only the affected neighbourhood: the person, the partner, the parents, the children and their partners. It keeps a set of *structural* candidates (rules 1 and 2).
- **Time trap:** "under 18" changes as time passes without a POST. Therefore:
  - the age rule (rule 3) is applied when the response is built, against an injected `Clock`;
  - an ADR records this, guarded by an ArchUnit rule: no `LocalDate.now()` outside the clock.
- **Domain without Spring,** guarded by ArchUnit. Pattern rules get jqwik property tests, for example "matches ⇔ brute-force check over the whole graph".
- **Fitness functions:**
  - a concurrency test, where parallel POSTs keep integrity intact;
  - PIT with a threshold, kept in but without letting it take over the plan;
  - optionally a simple load measurement as a performance indication, documented in the README.

## Time box

The estimate is 2 hours. Priority order:
1. step A;
2. step B;
3. scenarios 1–8;
4. scenario 9 (phase 2) only if there is time.

Record the time actually spent in the README.

## Verification

- `make commit-stage` is green: unit tests, `@SpringBootTest` integration tests, ArchUnit and PIT above the threshold.
- `make acceptance` is green against the image you just built, and the version check matches the commit.
- Manual check: `docker run` the image, then use `curl -i -X POST localhost:8080/api/v1/people -d @...` to rebuild the family from scenario 2 step by step: first 444, at the end 200 with a body.
- GitHub Actions: seen red once in the right stage, then green (`gh run watch <id> --exit-status`).
- `git log --oneline` shows small commits, one per ATDD step, each with "Seen red" and "Not verified" in the message.
