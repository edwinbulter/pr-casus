package nl.kabulter.threekids.store;

import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * All person records in memory, by ID (ADR 0005). Filtering happens in Java, never in a query.
 * Records are kept as they were posted; two reverse indexes derive the parents of a child from who names it as their
 * child, and the children of a parent from who names it as their parent (ADR 0008). Deleted IDs are ignored in every
 * answer (ADR 0011).
 */
@Repository
public class InMemoryPeople implements People {

    private final Map<Long, Person> byId = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> namedAsChildBy = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> namedAsParentBy = new ConcurrentHashMap<>();
    private final Set<Long> deleted = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized void save(Person person) {
        if (deleted.contains(person.id())) {
            return;
        }
        Person replaced = byId.put(person.id(), person);
        forgetClaimsOf(replaced);
        person.children().forEach(child -> remember(namedAsChildBy, child, person.id()));
        parentsIn(person).forEach(parent -> remember(namedAsParentBy, parent, person.id()));
    }

    @Override
    public synchronized void delete(long id) {
        deleted.add(id);
        forgetClaimsOf(byId.remove(id));
    }

    @Override
    public Optional<Person> find(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Set<Long> parentsOf(long child) {
        Set<Long> parents = new HashSet<>(namedAsChildBy.getOrDefault(child, Set.of()));
        find(child).ifPresent(record -> parents.addAll(parentsIn(record)));
        return withoutDeleted(child, parents);
    }

    @Override
    public Set<Long> childrenOf(long parent) {
        Set<Long> children = new HashSet<>(namedAsParentBy.getOrDefault(parent, Set.of()));
        find(parent).ifPresent(record -> children.addAll(record.children()));
        return withoutDeleted(parent, children);
    }

    @Override
    public Collection<Person> all() {
        return byId.values();
    }

    private void forgetClaimsOf(Person person) {
        if (person != null) {
            person.children().forEach(child -> forget(namedAsChildBy, child, person.id()));
            parentsIn(person).forEach(parent -> forget(namedAsParentBy, parent, person.id()));
        }
    }

    /** A deleted person has no relations, and is no relation of anyone (ADR 0011). */
    private Set<Long> withoutDeleted(long id, Set<Long> relations) {
        if (deleted.contains(id)) {
            return Set.of();
        }
        relations.removeAll(deleted);
        return relations;
    }

    private static void remember(Map<Long, Set<Long>> index, long about, long by) {
        index.computeIfAbsent(about, unused -> ConcurrentHashMap.newKeySet()).add(by);
    }

    private static void forget(Map<Long, Set<Long>> index, long about, long by) {
        Set<Long> claims = index.get(about);
        if (claims != null) {
            claims.remove(by);
            if (claims.isEmpty()) {
                index.remove(about);
            }
        }
    }

    private static List<Long> parentsIn(Person person) {
        return Stream.of(person.parent1(), person.parent2()).filter(Objects::nonNull).toList();
    }
}
