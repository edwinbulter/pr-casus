package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ADR 0010: the work of one registration does not grow with the number of people stored.
 */
class PeopleServiceWorkTests {

    private static final Clock TODAY = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC);

    private final CountingPeople store = new CountingPeople();
    private final PeopleService people = new PeopleService(store, TODAY);

    @Test
    void aRegistrationEvaluatesOnlyAFewCandidatesHoweverManyPeopleAreStored() {
        for (long id = 1; id <= 1_000; id++) {
            people.register(new Person(id, null, null, null, null, null, List.of()));
        }
        store.resetCount();

        people.register(new Person(1_001, null, null, null, null, null, List.of()));

        assertThat(store.childrenAsked()).as("candidates evaluated for one registration").isLessThanOrEqualTo(10);
    }
}
