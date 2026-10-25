package nl.kabulter.threekids.acceptance.drivers;

import java.time.LocalDate;
import java.util.List;

/**
 * A person record as the service receives and returns it. Unknown values are null.
 */
public record PersonRecord(
        long id,
        String name,
        LocalDate birthDate,
        Long parent1,
        Long parent2,
        Long partner,
        List<Long> children) {
}
