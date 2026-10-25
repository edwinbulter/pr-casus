package nl.kabulter.threekids.store;

import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryPeopleTests {

    private final InMemoryPeople people = new InMemoryPeople();

    @Test
    void keepsEverySavedPerson() {
        Person bart = new Person(1, "Bart", null, null, null, null, List.of());
        Person lisa = new Person(2, "Lisa", null, null, null, null, List.of());

        people.save(bart);
        people.save(lisa);

        assertThat(people.all()).containsExactlyInAnyOrder(bart, lisa);
    }
}
