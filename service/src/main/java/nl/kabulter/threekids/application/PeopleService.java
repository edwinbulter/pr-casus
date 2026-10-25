package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.Person;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Registers person records and answers which people currently match the three-kid family pattern.
 */
@Service
public class PeopleService {

    public List<Person> register(Person person) {
        return List.of();
    }
}
