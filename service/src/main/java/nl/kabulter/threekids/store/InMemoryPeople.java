package nl.kabulter.threekids.store;

import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All person records in memory, by ID (ADR 0005). Filtering happens in Java, never in a query.
 */
@Repository
public class InMemoryPeople implements People {

    private final Map<Long, Person> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Person person) {
        byId.put(person.id(), person);
    }

    @Override
    public Collection<Person> all() {
        return byId.values();
    }
}
