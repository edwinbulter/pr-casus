package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The pattern the service looks for: a person with a partner and exactly three children, who all have that partner
 * as mother or father, and at least one of whom is under 18. Children and parents come from the relations: what a
 * record says itself and what the other records say about it (ADR 0008). Only children whose record is known count.
 */
public final class ThreeKidFamily {

    private static final int CHILDREN = 3;
    private static final int ADULT_AGE = 18;

    private ThreeKidFamily() {
    }

    public static boolean isMatchedBy(Person person, Relations relations, LocalDate today) {
        return minorUntil(person, relations).filter(until -> until.isAfter(today)).isPresent();
    }

    /**
     * For a person with a partner and three known children who all have that partner as parent: the day its youngest
     * child with a known birth date turns 18. Before that day the person matches, from that day on it does not
     * (ADR 0010). Empty for anyone else, and when no child has a known birth date.
     */
    public static Optional<LocalDate> minorUntil(Person person, Relations relations) {
        Set<Long> childIds = relations.childrenOf(person.id());
        if (person.partner() == null || childIds.size() != CHILDREN) {
            return Optional.empty();
        }
        List<Person> children = childIds.stream()
                .map(relations::find)
                .flatMap(Optional::stream)
                .toList();
        if (children.size() != CHILDREN
                || !children.stream().allMatch(child -> relations.parentsOf(child.id()).contains(person.partner()))) {
            return Optional.empty();
        }
        return children.stream()
                .map(Person::birthDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(youngest -> youngest.plusYears(ADULT_AGE));
    }
}
