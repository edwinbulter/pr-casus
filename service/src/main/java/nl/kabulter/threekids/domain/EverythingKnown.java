package nl.kabulter.threekids.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Everything the service knows about a person: its record as posted, completed with the relations that other records
 * state about it (ADR 0008). Its own children come first, then the others by ID; an empty parent is filled with a
 * parent that names the person as child, by ID.
 */
public final class EverythingKnown {

    private EverythingKnown() {
    }

    public static Person about(Person person, Relations relations) {
        List<Long> children = new ArrayList<>(person.children());
        relations.childrenOf(person.id()).stream()
                .filter(child -> !children.contains(child))
                .sorted()
                .forEach(children::add);

        Iterator<Long> otherParents = relations.parentsOf(person.id()).stream()
                .filter(parent -> !parent.equals(person.parent1()) && !parent.equals(person.parent2()))
                .sorted()
                .iterator();
        Long parent1 = person.parent1() != null ? person.parent1() : next(otherParents);
        Long parent2 = person.parent2() != null ? person.parent2() : next(otherParents);

        return new Person(person.id(), person.name(), person.birthDate(), parent1, parent2, person.partner(),
                List.copyOf(children));
    }

    private static Long next(Iterator<Long> ids) {
        return ids.hasNext() ? ids.next() : null;
    }
}
