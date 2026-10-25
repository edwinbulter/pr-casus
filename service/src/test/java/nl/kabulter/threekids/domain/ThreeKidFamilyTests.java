package nl.kabulter.threekids.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ThreeKidFamilyTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);
    private static final LocalDate ADULT = LocalDate.of(1999, 4, 1);
    private static final LocalDate MINOR = LocalDate.of(2020, 1, 12);
    private static final long PARENT = 1, PARTNER = 2, FIRST = 3, SECOND = 4, THIRD = 5, SOMEONE_ELSE = 6;

    private final Map<Long, Person> known = new HashMap<>();

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
        known.put(SECOND, new Person(SECOND, null, ADULT, PARENT, SOMEONE_ELSE, null, List.of()));

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    @Test
    void thePartnerMayBeListedAsEitherParent() {
        knowChildren(ADULT, ADULT, MINOR);
        known.put(SECOND, new Person(SECOND, null, ADULT, PARTNER, PARENT, null, List.of()));

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isTrue();
    }

    @Test
    void aChildKnownOnlyByIdDoesNotCountAsHavingThePartnerAsParent() {
        knowChildren(ADULT, ADULT, MINOR);
        known.remove(FIRST);

        assertThat(isMatched(parent(PARTNER, List.of(FIRST, SECOND, THIRD)))).isFalse();
    }

    private boolean isMatched(Person person) {
        Relations relations = id -> Optional.ofNullable(known.get(id));
        return ThreeKidFamily.isMatchedBy(person, relations, TODAY);
    }

    private void knowChildren(LocalDate first, LocalDate second, LocalDate third) {
        known.put(FIRST, child(FIRST, first));
        known.put(SECOND, child(SECOND, second));
        known.put(THIRD, child(THIRD, third));
    }

    private static Person child(long id, LocalDate birthDate) {
        return new Person(id, null, birthDate, PARENT, PARTNER, null, List.of());
    }

    private static Person parent(Long partner, List<Long> children) {
        return new Person(PARENT, null, null, null, null, partner, children);
    }
}
