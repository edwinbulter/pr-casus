# 0011. Deleted IDs are treated as absent everywhere

Date: 2026-10-25
Status: Accepted

## Context
Phase 2 of the casus: `DELETE /api/v1/people` with an array of IDs. The service "must delete those IDs and remember them forever: if they show up later, silently ignore them for both storage and matching". The scenario "a deleted person no longer counts for matching" forces three choices, approved by the user:
1. the answer to a DELETE;
2. what references to a deleted ID in other records mean;
3. whether such references still appear in a response.

## Decision
1. A DELETE answers **204 No Content**: it removes people and has no list to return. The 200 and 444 of the casus belong to a POST.
2. A reference to a deleted ID counts as **absent**, as child, parent and partner. The records that contain it stay as posted (ADR 0008); only what is derived from them changes:
   - `InMemoryPeople.delete(id)` removes the record and its claims, and keeps the ID in a set of deleted IDs;
   - `parentsOf` and `childrenOf` never return a deleted ID, and a deleted ID has no parents or children.

   A partner check is not needed: no child can name a deleted partner as parent any longer, so a person with a deleted partner cannot match.
3. A response leaves out references to deleted IDs: `EverythingKnown` only shows the children and parents that `Relations` still knows.

Deleting re-evaluates the family of the deleted ID, collected before deleting (ADR 0010).

## Consequences
- The set of deleted IDs grows forever, as the casus asks ("remember them forever").
- A POST of a deleted ID is not yet handled explicitly; that is the next scenario.

## Guard
- Acceptance: `DeletionScenarios.aDeletedPersonNoLongerCountsForMatching`.
- `PeopleControllerTests.deletesThePeopleInTheRequestAndAnswers204`.
- `InMemoryPeopleTests`: a deleted person is not found, is no child or parent of anyone, and has no relations itself.
- `EverythingKnownTests.leavesOutTheChildrenAndParentsTheRelationsNoLongerKnow`.
- `IncrementalMatchingProperties`: random sequences now include deletions. Seen red by sabotage: the family collected after deleting, and no re-evaluation after deleting.
