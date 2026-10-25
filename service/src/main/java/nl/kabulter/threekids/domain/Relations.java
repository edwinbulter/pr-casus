package nl.kabulter.threekids.domain;

import java.util.Optional;

/**
 * Read-only view on the known people and how they are related, as the pattern needs it.
 */
public interface Relations {

    Optional<Person> find(long id);
}
