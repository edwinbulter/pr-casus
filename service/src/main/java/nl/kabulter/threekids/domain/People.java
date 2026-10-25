package nl.kabulter.threekids.domain;

import java.util.Collection;

/**
 * All person records the service knows. Saving a person with a known ID replaces the old record.
 */
public interface People extends Relations {

    void save(Person person);

    Collection<Person> all();
}
