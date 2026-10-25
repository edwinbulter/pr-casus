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

    @Test
    void findsASavedPersonById() {
        Person bart = new Person(1, "Bart", null, null, null, null, List.of());
        people.save(bart);

        assertThat(people.find(1)).contains(bart);
    }

    @Test
    void findsNobodyForAnUnknownId() {
        assertThat(people.find(1)).isEmpty();
    }

    @Test
    void theParentsOfAChildAreThoseItsOwnRecordNames() {
        people.save(new Person(3, "Bart", null, 1L, 2L, null, List.of()));

        assertThat(people.parentsOf(3)).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void theParentsOfAChildAlsoIncludeThoseWhoNameItAsTheirChild() {
        people.save(new Person(3, "Bart", null, null, null, null, List.of()));
        people.save(new Person(1, "Homer", null, null, null, 2L, List.of(3L)));

        assertThat(people.parentsOf(3)).containsExactly(1L);
    }

    @Test
    void aChildKnownOnlyByIdHasTheParentsWhoNameIt() {
        people.save(new Person(1, "Homer", null, null, null, 2L, List.of(3L)));
        people.save(new Person(2, "Marge", null, null, null, 1L, List.of(3L)));

        assertThat(people.parentsOf(3)).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void aReplacedPersonNoLongerCountsAsParentOfTheChildrenItDropped() {
        people.save(new Person(1, "Homer", null, null, null, 2L, List.of(3L)));
        people.save(new Person(1, "Homer", null, null, null, 2L, List.of()));

        assertThat(people.parentsOf(3)).isEmpty();
    }
}
