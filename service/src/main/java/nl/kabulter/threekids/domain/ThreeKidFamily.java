package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

/**
 * The pattern the service looks for: a person with a partner and exactly three children, at least one of them
 * under 18. A child counts as under 18 only when its record and birth date are known.
 */
public final class ThreeKidFamily {

    private static final int ADULT_AGE = 18;

    private ThreeKidFamily() {
    }

    public static boolean isMatchedBy(Person person, Function<Long, Optional<Person>> knownPerson, LocalDate today) {
        return person.partner() != null
                && person.children().size() == 3
                && person.children().stream()
                        .map(knownPerson)
                        .flatMap(Optional::stream)
                        .anyMatch(child -> isUnder18(child, today));
    }

    private static boolean isUnder18(Person child, LocalDate today) {
        return child.birthDate() != null && child.birthDate().plusYears(ADULT_AGE).isAfter(today);
    }
}
