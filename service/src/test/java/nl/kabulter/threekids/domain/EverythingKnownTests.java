package nl.kabulter.threekids.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EverythingKnownTests {

    @Test
    void addsTheChildrenWhoNameThePersonAfterItsOwnChildrenById() {
        Person homer = new Person(1, "Homer", null, null, null, 2L, List.of(7L));
        Relations relations = relations(Map.of(), Map.of(1L, Set.of(9L, 7L, 5L)));

        assertThat(EverythingKnown.about(homer, relations).children()).containsExactly(7L, 5L, 9L);
    }

    @Test
    void fillsAnEmptyParentWithAParentWhoNamesThePersonAsChild() {
        Person bart = new Person(3, "Bart", null, 2L, null, null, List.of());
        Relations relations = relations(Map.of(3L, Set.of(2L, 1L)), Map.of());

        Person known = EverythingKnown.about(bart, relations);

        assertThat(known.parent1()).isEqualTo(2L);
        assertThat(known.parent2()).isEqualTo(1L);
    }

    @Test
    void doesNotRepeatAParentTheRecordAlreadyNames() {
        Person bart = new Person(3, "Bart", null, 1L, null, null, List.of());
        Relations relations = relations(Map.of(3L, Set.of(1L, 2L)), Map.of());

        Person known = EverythingKnown.about(bart, relations);

        assertThat(known.parent1()).isEqualTo(1L);
        assertThat(known.parent2()).isEqualTo(2L);
    }

    @Test
    void fillsBothParentsByIdWhenTheRecordNamesNone() {
        Person bart = new Person(3, "Bart", null, null, null, null, List.of());
        Relations relations = relations(Map.of(3L, Set.of(2L, 1L)), Map.of());

        Person known = EverythingKnown.about(bart, relations);

        assertThat(known.parent1()).isEqualTo(1L);
        assertThat(known.parent2()).isEqualTo(2L);
    }

    @Test
    void leavesOutTheChildrenAndParentsTheRelationsNoLongerKnow() {
        Person homer = new Person(1, "Homer", null, 10L, 11L, 2L, List.of(3L, 4L));
        Relations relations = relations(Map.of(1L, Set.of(11L)), Map.of(1L, Set.of(4L)));

        Person known = EverythingKnown.about(homer, relations);

        assertThat(known.children()).containsExactly(4L);
        assertThat(known.parent1()).isNull();
        assertThat(known.parent2()).isEqualTo(11L);
    }

    @Test
    void keepsEverythingElseOfTheRecord() {
        Person homer = new Person(1, "Homer", java.time.LocalDate.of(1976, 5, 12), 10L, 11L, 2L, List.of());

        Relations relations = relations(Map.of(1L, Set.of(10L, 11L)), Map.of());

        assertThat(EverythingKnown.about(homer, relations)).isEqualTo(homer);
    }

    /** Like the store: the relations include what the person's own record says, unless that ID was deleted. */
    private static Relations relations(Map<Long, Set<Long>> parents, Map<Long, Set<Long>> children) {
        return new Relations() {
            @Override
            public Optional<Person> find(long id) {
                return Optional.empty();
            }

            @Override
            public Set<Long> parentsOf(long child) {
                return parents.getOrDefault(child, Set.of());
            }

            @Override
            public Set<Long> childrenOf(long parent) {
                return children.getOrDefault(parent, Set.of());
            }
        };
    }
}
