package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.store.InMemoryPeople;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeopleServiceTests {

    private static final long BART = 1, LISA = 2, MAGGIE = 3, MARGE = 4, HOMER = 5, LUANN = 6;

    private static final Clock TODAY = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC);

    private final PeopleService people = new PeopleService(new InMemoryPeople(), TODAY);

    @Test
    void aPersonWithTwoChildrenDoesNotMatch() {
        Person ada = new Person(42, "Ada Lovelace", LocalDate.of(1815, 12, 10), 1L, 2L, 99L, List.of(77L, 78L));

        assertThat(people.register(ada)).isEmpty();
    }

    @Test
    void answersEveryStoredPersonThatMatches() {
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, LocalDate.of(2020, 1, 12)));
        Person marge = parent(MARGE, LocalDate.of(1978, 3, 19), HOMER);

        assertThat(people.register(marge)).containsExactly(marge);

        Person homer = parent(HOMER, LocalDate.of(1976, 5, 12), MARGE);

        assertThat(people.register(homer)).containsExactlyInAnyOrder(homer, marge);
    }

    @Test
    void aFamilyWithOnlyAdultChildrenDoesNotMatch() {
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, LocalDate.of(2003, 1, 12)));
        people.register(parent(MARGE, LocalDate.of(1978, 3, 19), HOMER));

        assertThat(people.register(parent(HOMER, LocalDate.of(1976, 5, 12), MARGE))).isEmpty();
    }

    @Test
    void aFamilyWhereOneChildHasAnotherSecondParentDoesNotMatch() {
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(MAGGIE, LocalDate.of(2020, 1, 12)));
        people.register(new Person(LISA, null, LocalDate.of(2001, 5, 9), HOMER, LUANN, null, List.of()));
        people.register(new Person(MARGE, null, LocalDate.of(1978, 3, 19), null, null, HOMER, List.of(BART, MAGGIE)));

        assertThat(people.register(parent(HOMER, LocalDate.of(1976, 5, 12), MARGE))).isEmpty();
    }

    @Test
    void childrenWhoDoNotNameTheirParentsCountWhenTheParentsNameThem() {
        people.register(new Person(BART, null, LocalDate.of(1999, 4, 1), null, null, null, List.of()));
        people.register(new Person(LISA, null, LocalDate.of(2001, 5, 9), null, null, null, List.of()));
        people.register(new Person(MAGGIE, null, LocalDate.of(2020, 1, 12), null, null, null, List.of()));
        Person marge = parent(MARGE, LocalDate.of(1978, 3, 19), HOMER);
        people.register(marge);
        Person homer = parent(HOMER, LocalDate.of(1976, 5, 12), MARGE);

        assertThat(people.register(homer)).containsExactlyInAnyOrder(homer, marge);
    }

    @Test
    void parentsWhoDoNotNameTheirChildrenMatchWithTheChildrenWhoNameThem() {
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, LocalDate.of(2020, 1, 12)));
        Person marge = new Person(MARGE, null, LocalDate.of(1978, 3, 19), null, null, HOMER, List.of());
        Person margeWithChildren = new Person(MARGE, null, LocalDate.of(1978, 3, 19), null, null, HOMER,
                List.of(BART, LISA, MAGGIE));

        assertThat(people.register(marge)).containsExactly(margeWithChildren);

        Person homer = new Person(HOMER, null, LocalDate.of(1976, 5, 12), null, null, MARGE, List.of());
        Person homerWithChildren = new Person(HOMER, null, LocalDate.of(1976, 5, 12), null, null, MARGE,
                List.of(BART, LISA, MAGGIE));

        assertThat(people.register(homer)).containsExactlyInAnyOrder(homerWithChildren, margeWithChildren);
    }

    @Test
    void aCorrectedBirthDateCanEndAMatch() {
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, LocalDate.of(2020, 1, 12)));
        people.register(parent(MARGE, LocalDate.of(1978, 3, 19), HOMER));
        people.register(parent(HOMER, LocalDate.of(1976, 5, 12), MARGE));

        assertThat(people.register(child(MAGGIE, LocalDate.of(2003, 1, 12)))).isEmpty();
    }

    @Test
    void aFamilyWhoseYoungestChildTurns18TodayNoLongerMatches() {
        LocalDate today = LocalDate.now(TODAY);
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, today.minusYears(18)));
        people.register(parent(MARGE, LocalDate.of(1978, 3, 19), HOMER));

        assertThat(people.register(parent(HOMER, LocalDate.of(1976, 5, 12), MARGE))).isEmpty();
    }

    @Test
    void aFamilyWhoseYoungestChildTurns18TomorrowStillMatches() {
        LocalDate today = LocalDate.now(TODAY);
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, today.minusYears(18).plusDays(1)));
        people.register(parent(MARGE, LocalDate.of(1978, 3, 19), HOMER));

        assertThat(people.register(parent(HOMER, LocalDate.of(1976, 5, 12), MARGE))).hasSize(2);
    }

    @Test
    void aDeletedPersonNoLongerCountsForMatching() {
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, LocalDate.of(2020, 1, 12)));
        people.register(parent(MARGE, LocalDate.of(1978, 3, 19), HOMER));

        people.delete(List.of(MAGGIE));

        assertThat(people.register(parent(HOMER, LocalDate.of(1976, 5, 12), MARGE))).isEmpty();
    }

    @Test
    void deletingAMatchingPersonsChildEndsTheMatchOfTheOthers() {
        people.register(child(BART, LocalDate.of(1999, 4, 1)));
        people.register(child(LISA, LocalDate.of(2001, 5, 9)));
        people.register(child(MAGGIE, LocalDate.of(2020, 1, 12)));
        people.register(parent(MARGE, LocalDate.of(1978, 3, 19), HOMER));
        people.register(parent(HOMER, LocalDate.of(1976, 5, 12), MARGE));

        people.delete(List.of(BART));

        assertThat(people.register(new Person(99, null, null, null, null, null, List.of()))).isEmpty();
    }

    private static Person child(long id, LocalDate birthDate) {
        return new Person(id, null, birthDate, HOMER, MARGE, null, List.of());
    }

    private static Person parent(long id, LocalDate birthDate, long partner) {
        return new Person(id, null, birthDate, null, null, partner, List.of(BART, LISA, MAGGIE));
    }
}
