package nl.kabulter.threekids.domain;

import java.util.Collection;
import java.util.Optional;

/**
 * All person records the service knows. Saving a person with a known ID replaces the old record.
 */
public interface People {

    void save(Person person);

    Optional<Person> find(long id);

    Collection<Person> all();
}
