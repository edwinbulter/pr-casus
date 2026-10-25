# 0005. Person records are kept in memory

Date: 2026-10-25
Status: Accepted

## Context
The scenario "a parent with a partner and three children, one of them under 18, matches" is the first that needs stored people: when Homer is registered, Marge must still be known. The casus demands that all filtering happens in Java ("no SQL WHERE tricks"), and it is evaluated on performance under load. Nothing in the casus asks for data that survives a restart.

Options:
1. **In memory**, a `ConcurrentHashMap` by ID: no I/O, lookups by ID in constant time, no query language at all.
2. **A database** (for example PostgreSQL or H2) with a repository: survives a restart, but every request pays for I/O and mapping, and the rule "no SQL WHERE" means the database would only be used as a key-value store.

## Decision
- The domain defines the port `People` (`save`, `all`); `store/InMemoryPeople` implements it with a `ConcurrentHashMap<Long, Person>`.
- Saving a person with a known ID replaces the old record.
- All matching happens in Java, in the domain (`ThreeKidFamily`).

## Consequences
- Fast and simple; the service needs no database, also not in the acceptance environment.
- The data is lost on a restart, and one instance holds all data: the service does not scale out horizontally. Both are acceptable for the casus; a persistent store can replace `InMemoryPeople` behind the same port.
- Memory grows with the number of people; one record is small (an ID, a name, a date and a few references).
- For now every registration checks all stored people: correct, but linear in the number of people. Incremental matching gets its own decision.

## Guard
- `InMemoryPeopleTests` and `PeopleServiceTests` (a family is still known at the next registration).
- The service has no database dependency in `service/pom.xml`; an ArchUnit rule (no `java.sql` or JPA in the service) follows with ArchUnit.
