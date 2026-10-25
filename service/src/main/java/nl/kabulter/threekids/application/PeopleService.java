package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.EverythingKnown;
import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.domain.ThreeKidFamily;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Registers person records and answers which people currently match the three-kid family pattern.
 * A registration only re-evaluates the family of the registered person (ADR 0010).
 */
@Service
public class PeopleService {

    private final People people;
    private final Clock clock;
    private final Map<Long, LocalDate> minorUntilById = new HashMap<>();
    private final NavigableMap<LocalDate, Set<Long>> candidatesByMinorUntil = new TreeMap<>();

    public PeopleService(People people, Clock clock) {
        this.people = people;
        this.clock = clock;
    }

    public synchronized List<Person> register(Person person) {
        Set<Long> affected = new HashSet<>();
        addFamilyOf(person.id(), affected);
        people.save(person);
        addFamilyOf(person.id(), affected);
        affected.forEach(this::reevaluate);

        LocalDate today = LocalDate.now(clock);
        return candidatesByMinorUntil.tailMap(today, false).values().stream()
                .flatMap(Set::stream)
                .map(people::find)
                .flatMap(Optional::stream)
                .map(match -> EverythingKnown.about(match, people))
                .toList();
    }

    /** Whose match can change when this person's record changes: the person, its parents and the parents of its children. */
    private void addFamilyOf(long id, Set<Long> affected) {
        affected.add(id);
        affected.addAll(people.parentsOf(id));
        people.childrenOf(id).forEach(child -> affected.addAll(people.parentsOf(child)));
    }

    private void reevaluate(long id) {
        LocalDate previous = minorUntilById.remove(id);
        if (previous != null) {
            Set<Long> candidates = candidatesByMinorUntil.get(previous);
            candidates.remove(id);
            if (candidates.isEmpty()) {
                candidatesByMinorUntil.remove(previous);
            }
        }
        people.find(id)
                .flatMap(person -> ThreeKidFamily.minorUntil(person, people))
                .ifPresent(until -> {
                    minorUntilById.put(id, until);
                    candidatesByMinorUntil.computeIfAbsent(until, unused -> new HashSet<>()).add(id);
                });
    }
}
