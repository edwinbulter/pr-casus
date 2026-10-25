# 0008. Parent and child relations are derived in both directions

Date: 2026-10-25
Status: Accepted

## Context
The casus demands bidirectional integrity: if A says B is a child, B must list A as a parent ("enforce or auto-repair"). The scenario "children who do not name their parents still count when the parents name them" forces the choice. The answer to a registration can only be 200 or 444, and references to people who were never posted are explicitly allowed.

Options:
1. **Auto-repair as a derived view.** Records are kept as they were posted. Parents and children are derived in both directions from what all records say.
2. **Auto-repair by changing the stored records.** Every registration adds the missing side to the records of the people it names.
3. **Enforce:** reject an inconsistent record.

## Decision
We choose **option 1**, because it keeps what was posted apart from what was derived. An update then needs no undo: a relation that was only derived disappears as soon as nobody states it anymore. And a child known only by ID can get its parents from the records that name it.

- The domain port `Relations` (read-only) offers `parentsOf(child)`: the parents its own record names, plus everyone who names it as their child.
- `InMemoryPeople` keeps a reverse index, child → who names it as a child, so `parentsOf` is a lookup by ID. `save` updates record and index together (`synchronized`) and removes the old claims of a replaced person.
- `ThreeKidFamily` takes the parents of a child from `Relations`, not from the child's record.

Option 2 loses the difference between posted and derived data, which updates (scenario 7) need. Option 3 does not fit: there is no answer to reject with, and unknown references are allowed.

## Consequences
- The response still returns the records as posted. Whether it should also show derived relations ("everything we know") is decided with the scenario where a parent does not name its children.
- The other direction, the children of a person derived from who names them as parent, follows with its own scenario.
- A conflict, a child with two own parents named as a child by a third person, is not yet decided; for now the third person also counts as a parent.
- Writes are serialized; reads are lock-free and may see a registration that is still in progress.

## Guard
- Acceptance: `IntegrityScenarios.childrenWhoDoNotNameTheirParentsStillCountWhenTheParentsNameThem`.
- `InMemoryPeopleTests`: parents from the own record, from who names the child, for a child known only by ID, and after replacing a person. Seen red by sabotage: without removing old claims, and without `parent2`.
- `ThreeKidFamilyTests.theParentsOfAChildAreThoseTheRelationsKnowNotOnlyThoseInItsRecord`.
