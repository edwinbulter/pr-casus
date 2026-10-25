package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.store.InMemoryPeople;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeopleServiceTests {

    private static final long BART = 1, LISA = 2, MAGGIE = 3, MARGE = 4, HOMER = 5;

    private final PeopleService people = new PeopleService(new InMemoryPeople());

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

    private static Person child(long id, LocalDate birthDate) {
        return new Person(id, null, birthDate, HOMER, MARGE, null, List.of());
    }

    private static Person parent(long id, LocalDate birthDate, long partner) {
        return new Person(id, null, birthDate, null, null, partner, List.of(BART, LISA, MAGGIE));
    }
}
