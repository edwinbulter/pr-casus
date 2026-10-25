package nl.kabulter.threekids.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ThreeKidFamilyTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);
    private static final LocalDate ADULT = LocalDate.of(1999, 4, 1);
    private static final LocalDate MINOR = LocalDate.of(2020, 1, 12);
    private static final long PARENT = 1, PARTNER = 2, FIRST = 3, SECOND = 4, THIRD = 5, SOMEONE_ELSE = 6;

    private final Map<Long, Person> known = new HashMap<>();
    private final Map<Long, Set<Long>> parents = new HashMap<>();
    private final Map<Long, Set<Long>> children = new HashMap<>();
    private final Relations relations = new Relations() {
        @Override
        public Optional<Person> find(long id) {
            return Optional.ofNullable(known.get(id));
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

    @Test
    void aPersonWithAPartnerAndThreeChildrenOneOfThemUnder18Matches() {
        knowChildren(ADULT, ADULT, MINOR);

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isTrue();
    }

    @Test
    void aPersonWithoutAPartnerDoesNotMatch() {
        knowChildren(ADULT, ADULT, MINOR);

        assertThat(isMatched(parent(null, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void aPersonWithTwoChildrenDoesNotMatch() {
        knowChildren(ADULT, MINOR, MINOR);

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND)))).isFalse();
    }

    @Test
    void aPersonWhoseChildrenAreAllAdultsDoesNotMatch() {
        knowChildren(ADULT, ADULT, ADULT);

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void aChildWhoTurns18TodayIsNoLongerUnder18() {
        knowChildren(ADULT, ADULT, TODAY.minusYears(18));

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void aChildWhoTurns18TomorrowIsStillUnder18() {
        knowChildren(ADULT, ADULT, TODAY.minusYears(18).plusDays(1));

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isTrue();
    }

    @Test
    void aChildWithoutABirthDateDoesNotCountAsUnder18() {
        knowChildren(ADULT, ADULT, null);

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void aChildKnownOnlyByIdDoesNotCountAsUnder18() {
        knowChildren(ADULT, ADULT, MINOR);
        known.remove(THIRD);

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void aChildWithAnotherSecondParentDoesNotMatch() {
        knowChildren(ADULT, ADULT, MINOR);
        know(new Person(SECOND, null, ADULT, PARENT, SOMEONE_ELSE, null, List.of()));

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void thePartnerMayBeListedAsEitherParent() {
        knowChildren(ADULT, ADULT, MINOR);
        know(new Person(SECOND, null, ADULT, PARTNER, PARENT, null, List.of()));

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isTrue();
    }

    @Test
    void aChildKnownOnlyByIdDoesNotCountAsHavingThePartnerAsParent() {
        knowChildren(ADULT, ADULT, MINOR);
        known.remove(FIRST);

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void theParentsOfAChildAreThoseTheRelationsKnowNotOnlyThoseInItsRecord() {
        knowChildren(ADULT, ADULT, MINOR);
        known.put(THIRD, new Person(THIRD, null, MINOR, null, null, null, List.of()));

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isTrue();
    }

    @Test
    void theChildrenOfAPersonAreThoseTheRelationsKnowNotOnlyThoseInItsRecord() {
        knowChildren(ADULT, ADULT, MINOR);
        children.put(PARENT, Set.of(FIRST, SECOND, THIRD));

        assertThat(isMatched(parent(PARTNER, List.of()))).isTrue();
    }

    @Test
    void aCandidateHasAMinorChildUntilItsYoungestChildTurns18() {
        knowChildren(ADULT, MINOR, ADULT);

        assertThat(minorUntil(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).contains(MINOR.plusYears(18));
    }

    @Test
    void aCandidateWithOnlyAdultChildrenHadAMinorChildUntilItsYoungestChildTurned18() {
        LocalDate youngest = ADULT.plusYears(1);
        knowChildren(ADULT, youngest, ADULT);

        assertThat(minorUntil(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).contains(youngest.plusYears(18));
    }

    @Test
    void aCandidateWithoutAnyKnownBirthDateNeverHasAMinorChild() {
        knowChildren(null, null, null);

        assertThat(minorUntil(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isEmpty();
    }

    @Test
    void someoneWhoIsNoCandidateHasNoDate() {
        knowChildren(ADULT, MINOR, ADULT);

        assertThat(minorUntil(parent(null, List.of(FIRST, SECOND, THIRD)))).isEmpty();
    }

    private Optional<LocalDate> minorUntil(Person person) {
        children.putIfAbsent(person.id(), Set.copyOf(person.children()));
        return ThreeKidFamily.minorUntil(person, relations);
    }

    /** Unless a test says otherwise, the relations know the children of a person as its own record states them. */
    private boolean isMatched(Person person) {
        children.putIfAbsent(person.id(), Set.copyOf(person.children()));
        return ThreeKidFamily.isMatchedBy(person, relations, TODAY);
    }

    private void knowChildren(LocalDate first, LocalDate second, LocalDate third) {
        know(child(FIRST, first));
        know(child(SECOND, second));
        know(child(THIRD, third));
    }

    /** A known child; the relations know its parents as its own record states them. */
    private void know(Person child) {
        known.put(child.id(), child);
        parents.put(child.id(), Set.of(child.parent1(), child.parent2()));
    }

    private static Person child(long id, LocalDate birthDate) {
        return new Person(id, null, birthDate, PARENT, PARTNER, null, List.of());
    }

    private static Person parent(Long partner, List<Long> children) {
        return new Person(PARENT, null, null, null, null, partner, children);
    }
}
