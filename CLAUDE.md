# Three-Kid Family Challenge

The casus "Three-Kid Family Challenge" ([doc/casus.md](doc/casus.md); the original PDF is local only, not in Git), built with Dave Farley's ATDD way of working. The goal and the approach are in [README.md](README.md). The initial plan and the scenario backlog are in [doc/initial-plan.md](doc/initial-plan.md).

## ATDD-profiel

Werkwijze: skill `atdd` (`.claude/skills/atdd/`). This profile holds what is specific to this project.

> **Step 0 (walking skeleton) is done:** the commands below are verified, locally and in CI. PIT, jqwik and ArchUnit come with their ADR.

**Language:** English, in the code (domain terms), the scenarios, the ADRs, the docs and the commit messages. The skills `atdd` and `uitleg` are an unchanged copy from [banksim-mse](https://github.com/edwinbulter/banksim-mse) and stay in Dutch.

**Stack:**
- Java 21 and Spring Boot 4 (Maven wrapper, Jib);
- JUnit 5, jqwik, ArchUnit, PIT and Testcontainers;
- GitHub Actions.

New libraries or tools only with an ADR.

**Commands** (`make help` shows them all)
- Commit stage: `make commit-stage` (`mvnw verify`: unit and integration tests; the acceptance module only compiles).
- Build the image: `make image`, which builds `three-kids:<version>` with Jib in the local Docker daemon. It skips the tests: run the commit stage first.
- Acceptance tests: `make acceptance` (`image` + `acceptance-tests`). `make acceptance-tests` alone tests the image of the current version, which must exist.
- One scenario: `./mvnw -B -pl acceptance verify -DskipITs=false -Dapp.version=$(make -s version) -Dit.test=<ScenarioClass>`.
- Which version runs: `make version` gives the version of the working tree (short commit hash, `-dirty` with uncommitted changes); `GET /api/version` gives the version of the running service.
- Run the service: `make run` (http://localhost:8080).
- Measure the time per registration: `make perf` (tag `perf`, excluded from the commit stage; sizes with `PERF_SIZES=10000,100000`). The README holds the results.
- Pipeline: `gh run list --limit 3` and `gh run watch <id> --exit-status`.

**Where things go**

| Change | Where | Test |
| --- | --- | --- |
| Scenario (layer 1) | `acceptance/src/test/java/.../scenarios/` | JUnit 5 against the container |
| DSL (layer 2) | `acceptance/src/test/java/.../dsl/` | via the scenarios |
| Protocol driver (layer 3) | `acceptance/src/test/java/.../drivers/` (interface), `.../drivers/http/` | via the scenarios |
| Domain (family graph, pattern) | `service/src/main/java/.../domain/` (no Spring) | JUnit, jqwik |
| API and application | `service/src/main/java/.../api`, `.../application` | `@WebMvcTest`; `@SpringBootTest` with MockMvc (integration) |
| Architecture rules | `service/src/test/java/.../architecture/` | ArchUnit |
| Performance | `PeopleServiceWorkTests` (work per registration), `IncrementalMatchingProperties` (jqwik), `RegistrationMeasurement` (`make perf`) | commit stage, except the measurement |
| Pipeline | `.github/workflows/ci.yml` | seen red once, then green |
| ADR | `doc/adr/NNNN-title.md` | – |

**Target environment:** a local Docker container and the GitHub Actions runner. No Kubernetes.

**Conventions**
- Commit message headings: **Seen red** (message and reason), **Sabotage** (if done) and **Not verified**.
- One ATDD step per small commit: the evaluation looks at the Git history.
- Never commit or push without permission. A push to `main` starts the pipeline.
- No commit prefix, unless the user names one.

**Pitfalls**
- Commit stage and image are separate: `make image` builds with `-DskipTests`, and `make acceptance-tests` without `image` tests whatever image has the current version tag. After a code change use `make acceptance`; the version check catches an image of another commit, not one of an older state of the same dirty tree.
- HTTP status 444 is not in Spring's `HttpStatus`: use `ResponseEntity.status(444)`.
- "Under 18" changes as time passes without a POST. Never cache the age check; use the injected `Clock`, never `LocalDate.now()`.
- `DELETE /api/v1/people` carries a JSON body: check that the HTTP driver really sends it.

**Backlog and background**
- Scenario backlog and the domain ambiguities to approve: `doc/initial-plan.md`, section "Step C".
- Design and performance ADR candidates: `doc/initial-plan.md`, section "Design and performance".
- Progress: the status in `README.md`, the existing scenarios and the ADRs in `doc/adr/`.
- Explaining a part of the system: skill `uitleg` (`/uitleg <topic>`).
