package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.domain.ThreeKidFamily;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Registers person records and answers which people currently match the three-kid family pattern.
 */
@Service
public class PeopleService {

    private final People people;

    public PeopleService(People people) {
        this.people = people;
    }

    public List<Person> register(Person person) {
        people.save(person);
        return people.all().stream()
                .filter(ThreeKidFamily::isMatchedBy)
                .toList();
    }
}
