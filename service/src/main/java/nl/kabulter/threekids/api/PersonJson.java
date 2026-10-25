package nl.kabulter.threekids.api;

import nl.kabulter.threekids.domain.Person;

import java.time.LocalDate;
import java.util.List;

/**
 * A person record as posted to and returned by /api/v1/people. Relatives are references: { "id": n }.
 */
record PersonJson(
        long id,
        String name,
        LocalDate birthDate,
        Reference parent1,
        Reference parent2,
        Reference partner,
        List<Reference> children) {

    record Reference(long id) {
    }

    static PersonJson from(Person person) {
        return new PersonJson(person.id(), person.name(), person.birthDate(),
                referenceTo(person.parent1()), referenceTo(person.parent2()), referenceTo(person.partner()),
                person.children().stream().map(Reference::new).toList());
    }

    Person toPerson() {
        return new Person(id, name, birthDate, idOf(parent1), idOf(parent2), idOf(partner),
                children == null ? List.of() : children.stream().map(Reference::id).toList());
    }

    private static Reference referenceTo(Long id) {
        return id == null ? null : new Reference(id);
    }

    private static Long idOf(Reference reference) {
        return reference == null ? null : reference.id();
    }
}
