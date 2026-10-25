package nl.kabulter.threekids.store;

import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All person records in memory, by ID (ADR 0005). Filtering happens in Java, never in a query.
 * Records are kept as they were posted; a reverse index derives the parents of a child from who names it
 * as their child (ADR 0008).
 */
@Repository
public class InMemoryPeople implements People {

    private final Map<Long, Person> byId = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> namedAsChildBy = new ConcurrentHashMap<>();

    @Override
    public synchronized void save(Person person) {
        Person replaced = byId.put(person.id(), person);
        if (replaced != null) {
            replaced.children().forEach(child -> forget(child, replaced.id()));
        }
        person.children().forEach(child ->
                namedAsChildBy.computeIfAbsent(child, unused -> ConcurrentHashMap.newKeySet()).add(person.id()));
    }

    @Override
    public Optional<Person> find(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Set<Long> parentsOf(long child) {
        Set<Long> parents = new HashSet<>(namedAsChildBy.getOrDefault(child, Set.of()));
        find(child).ifPresent(record -> {
            addIfKnown(parents, record.parent1());
            addIfKnown(parents, record.parent2());
        });
        return parents;
    }

    @Override
    public Collection<Person> all() {
        return byId.values();
    }

    private void forget(long child, long parent) {
        Set<Long> parents = namedAsChildBy.get(child);
        if (parents != null) {
            parents.remove(parent);
            if (parents.isEmpty()) {
                namedAsChildBy.remove(child);
            }
        }
    }

    private static void addIfKnown(Set<Long> ids, Long id) {
        if (id != null) {
            ids.add(id);
        }
    }
}
