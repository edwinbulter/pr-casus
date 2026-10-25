package nl.kabulter.threekids.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Everything the service knows about a person: its record as posted, completed with the relations that other records
 * state about it (ADR 0008). Its own children come first, then the others by ID; an empty parent is filled with a
 * parent that names the person as child, by ID. Children and parents the relations no longer know, such as deleted
 * IDs, are left out (ADR 0011).
 */
public final class EverythingKnown {

    private EverythingKnown() {
    }

    public static Person about(Person person, Relations relations) {
        Set<Long> knownChildren = relations.childrenOf(person.id());
        List<Long> children = new ArrayList<>(person.children().stream().filter(knownChildren::contains).toList());
        knownChildren.stream()
                .filter(child -> !children.contains(child))
                .sorted()
                .forEach(children::add);

        Set<Long> knownParents = relations.parentsOf(person.id());
        Long ownParent1 = ifKnown(person.parent1(), knownParents);
        Long ownParent2 = ifKnown(person.parent2(), knownParents);
        Iterator<Long> otherParents = knownParents.stream()
                .filter(parent -> !parent.equals(ownParent1) && !parent.equals(ownParent2))
                .sorted()
                .iterator();
        Long parent1 = ownParent1 != null ? ownParent1 : next(otherParents);
        Long parent2 = ownParent2 != null ? ownParent2 : next(otherParents);

        return new Person(person.id(), person.name(), person.birthDate(), parent1, parent2, person.partner(),
                List.copyOf(children));
    }

    private static Long ifKnown(Long id, Set<Long> known) {
        return id != null && known.contains(id) ? id : null;
    }

    private static Long next(Iterator<Long> ids) {
        return ids.hasNext() ? ids.next() : null;
    }
}
