package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeopleServiceTests {

    private final PeopleService people = new PeopleService();

    @Test
    void aPersonWithTwoChildrenDoesNotMatch() {
        Person ada = new Person(42, "Ada Lovelace", LocalDate.of(1815, 12, 10), 1L, 2L, 99L, List.of(77L, 78L));

        assertThat(people.register(ada)).isEmpty();
    }
}
