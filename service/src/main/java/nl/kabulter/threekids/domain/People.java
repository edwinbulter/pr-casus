package nl.kabulter.threekids.domain;

import java.util.Collection;

/**
 * All person records the service knows. Saving a person with a known ID replaces the old record. A deleted ID is
 * absent from every answer: as a person, and as a parent or child of anyone.
 */
public interface People extends Relations {

    void save(Person person);

    /** Removes the person and ignores its ID from now on, everywhere (ADR 0011). */
    void delete(long id);

    Collection<Person> all();
}
