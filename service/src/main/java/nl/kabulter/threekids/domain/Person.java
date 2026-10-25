package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * A person record. Relatives are referenced by ID and may be unknown to the service; unknown values are null.
 */
public record Person(
        long id,
        String name,
        LocalDate birthDate,
        Long parent1,
        Long parent2,
        Long partner,
        List<Long> children) {
}
