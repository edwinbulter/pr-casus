# 0001. Record architecture decisions as ADRs

Date: 2026-10-25
Status: Accepted

## Context
The casus is evaluated on architecture and docs. Choices such as the store, the matching strategy and the test setup must be traceable: why they were made, and what would break if we deviate from them. The way of working (skill `atdd`) asks for an ADR at the last responsible moment, when a scenario cannot continue without the choice.

## Decision
- Every architecture decision gets an ADR in `doc/adr/NNNN-title.md`, in the format of Michael Nygard with an extra section **Guard**.
- One decision per ADR. An accepted ADR is never rewritten; a new decision replaces it.
- The ADR goes into the same commit as the code that introduces the decision.

## Consequences
- The history of decisions is readable next to the code, and every ADR says how it is enforced.
- It costs a short document per decision. Small choices that the code explains itself do not get an ADR.

## Guard
No automated check. Every iteration report lists new and proposed ADRs; a new library or tool without an ADR is a review finding.
