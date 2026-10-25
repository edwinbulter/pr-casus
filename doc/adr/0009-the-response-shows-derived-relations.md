# 0009. The response shows everything known, including derived relations

Date: 2026-10-25
Status: Accepted

## Context
The casus says: "We should respond with everything we know about the matching people (you might not know their name or birthdate yet)". With relations derived in both directions (ADR 0008), the service can know more about a person than its own record says. The scenario "parents who do not name their children still match when the children name them" forces the choice: Homer posts no children, but three children name him as parent.

Options:
1. **Including derived relations:** a matching person is returned with its record as posted, completed with the children and parents that other records state.
2. **As registered:** a matching person is returned exactly as it was posted.

## Decision
We choose **option 1** (chosen by the user), because the casus asks for everything the service knows.

- `EverythingKnown.about(person, relations)` builds the returned person:
  - children: its own children first, in the posted order, then the other children by ID;
  - parents: its own `parent1` and `parent2`; an empty one is filled with a parent who names the person as child, by ID, never repeating a parent the record already names;
  - everything else as posted.
- `PeopleService.register` returns the matching people through `EverythingKnown`; the store keeps the records as posted.

## Consequences
- The response can contain relations nobody posted for this person; that is the point.
- In the scenarios where every record names its relations itself, nothing changes.
- With more than two derived parents (the open backlog item on conflicting parents), only two fit in `parent1` and `parent2`; the others are left out until that item is decided.

## Guard
- Acceptance: `IntegrityScenarios.parentsWhoDoNotNameTheirChildrenStillMatchWhenTheChildrenNameThem` expects Homer and Marge with children Bart, Lisa and Maggie.
- `EverythingKnownTests`: order of the children, filling empty parents, no repeated parent, the rest unchanged. Seen red by sabotage: without the birth date, without sorting, without excluding the own `parent1`.
