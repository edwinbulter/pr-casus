package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The pattern the service looks for: a person with a partner and exactly three children, who all have that partner
 * as mother or father, and at least one of whom is under 18. The parents of a child come from the relations: what
 * its own record says and who names it as a child (ADR 0008). Only children whose record is known count.
 */
public final class ThreeKidFamily {

    private static final int CHILDREN = 3;
    private static final int ADULT_AGE = 18;

    private ThreeKidFamily() {
    }

    public static boolean isMatchedBy(Person person, Relations relations, LocalDate today) {
        if (person.partner() == null || person.children().size() != CHILDREN) {
            return false;
        }
        List<Person> children = person.children().stream()
                .map(relations::find)
                .flatMap(Optional::stream)
                .toList();
        return children.size() == CHILDREN
                && children.stream().allMatch(child -> relations.parentsOf(child.id()).contains(person.partner()))
                && children.stream().anyMatch(child -> isUnder18(child, today));
    }

    private static boolean isUnder18(Person child, LocalDate today) {
        return child.birthDate() != null && child.birthDate().plusYears(ADULT_AGE).isAfter(today);
    }
}
