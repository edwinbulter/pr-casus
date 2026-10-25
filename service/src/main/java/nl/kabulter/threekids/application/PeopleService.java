package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.EverythingKnown;
import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.domain.ThreeKidFamily;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Registers person records and answers which people currently match the three-kid family pattern.
 */
@Service
public class PeopleService {

    private final People people;
    private final Clock clock;

    public PeopleService(People people, Clock clock) {
        this.people = people;
        this.clock = clock;
    }

    public List<Person> register(Person person) {
        people.save(person);
        LocalDate today = LocalDate.now(clock);
        return people.all().stream()
                .filter(candidate -> ThreeKidFamily.isMatchedBy(candidate, people, today))
                .map(match -> EverythingKnown.about(match, people))
                .toList();
    }
}
