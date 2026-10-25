package nl.kabulter.threekids.domain;

import java.util.Optional;
import java.util.Set;

/**
 * Read-only view on the known people and how they are related, as the pattern needs it.
 */
public interface Relations {

    Optional<Person> find(long id);

    /** The parents of a child: those its own record names, and those who name it as their child. */
    Set<Long> parentsOf(long child);

    /** The children of a person: those its own record names, and those who name it as a parent. */
    Set<Long> childrenOf(long parent);
}
