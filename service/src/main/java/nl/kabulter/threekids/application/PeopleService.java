package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.EverythingKnown;
import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.domain.ThreeKidFamily;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Registers person records and answers which people currently match the three-kid family pattern.
 * A registration only re-evaluates the family of the registered person (ADR 0010).
 */
@Service
public class PeopleService {

    private final People people;
    private final Clock clock;
    private final MatchCandidates candidates = new MatchCandidates();

    public PeopleService(People people, Clock clock) {
        this.people = people;
        this.clock = clock;
    }

    public synchronized List<Person> register(Person person) {
        Set<Long> affected = new HashSet<>();
        addFamilyOf(person.id(), affected);
        people.save(person);
        addFamilyOf(person.id(), affected);
        reevaluate(affected);

        return candidates.matchingOn(LocalDate.now(clock))
                .map(people::find)
                .flatMap(Optional::stream)
                .map(match -> EverythingKnown.about(match, people))
                .toList();
    }

    /** Deletes these people and ignores their IDs from now on (ADR 0011); their families are re-evaluated. */
    public synchronized void delete(List<Long> ids) {
        Set<Long> affected = new HashSet<>();
        for (long id : ids) {
            addFamilyOf(id, affected);
            people.delete(id);
        }
        reevaluate(affected);
    }

    private void reevaluate(Set<Long> affected) {
        affected.forEach(id -> candidates.update(id,
                people.find(id).flatMap(known -> ThreeKidFamily.minorUntil(known, people))));
    }

    /** Whose match can change when this person's record changes: the person, its parents and the parents of its children. */
    private void addFamilyOf(long id, Set<Long> affected) {
        affected.add(id);
        affected.addAll(people.parentsOf(id));
        people.childrenOf(id).forEach(child -> affected.addAll(people.parentsOf(child)));
    }
}
