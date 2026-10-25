package nl.kabulter.threekids.application;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.statistics.Statistics;
import nl.kabulter.threekids.domain.EverythingKnown;
import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.domain.ThreeKidFamily;
import nl.kabulter.threekids.store.InMemoryPeople;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ADR 0010: after every registration, the service answers exactly what checking all stored people would answer,
 * also after deletions (ADR 0011).
 */
class IncrementalMatchingProperties {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final long IDS = 6;

    sealed interface Operation permits Register, Delete {
    }

    record Register(Person person) implements Operation {
    }

    record Delete(List<Long> ids) implements Operation {
    }

    @Property(tries = 5000)
    void answersWhatCheckingEveryoneWouldAnswer(@ForAll("operations") List<Operation> operations) {
        PeopleService service = new PeopleService(new InMemoryPeople(), CLOCK);
        InMemoryPeople reference = new InMemoryPeople();

        boolean someoneMatched = false;
        for (Operation operation : operations) {
            switch (operation) {
                case Delete delete -> {
                    service.delete(delete.ids());
                    delete.ids().forEach(reference::delete);
                }
                case Register register -> {
                    Set<Person> answer = Set.copyOf(service.register(register.person()));

                    reference.save(register.person());
                    Set<Person> everyoneChecked = reference.all().stream()
                            .filter(candidate -> ThreeKidFamily.isMatchedBy(candidate, reference, TODAY))
                            .map(match -> EverythingKnown.about(match, reference))
                            .collect(Collectors.toSet());

                    assertThat(answer).as("answer after %s", operations).isEqualTo(everyoneChecked);
                    someoneMatched |= !answer.isEmpty();
                }
            }
        }
        Statistics.label("sequences").collect(someoneMatched ? "with a match" : "without a match");
        Statistics.label("deletions").collect(operations.stream().anyMatch(Delete.class::isInstance) ? "with" : "without");
    }

    @Provide
    Arbitrary<List<Operation>> operations() {
        Arbitrary<Operation> register = person().map(Register::new);
        Arbitrary<Operation> delete = Arbitraries.longs().between(1, IDS + 1).list().ofMinSize(1).ofMaxSize(2)
                .map(Delete::new);
        return Arbitraries.frequencyOf(
                net.jqwik.api.Tuple.of(9, register),
                net.jqwik.api.Tuple.of(1, delete)).list().ofMinSize(1).ofMaxSize(25);
    }

    private static Arbitrary<Person> person() {
        Arbitrary<Long> id = Arbitraries.longs().between(1, IDS);
        Arbitrary<Long> reference = Arbitraries.longs().between(1, IDS + 1).injectNull(0.4);
        Arbitrary<LocalDate> birthDate = Arbitraries.of(LocalDate.of(1970, 1, 1), LocalDate.of(2015, 6, 1))
                .injectNull(0.2);
        Arbitrary<List<Long>> children = Arbitraries.longs().between(1, IDS + 1).list().ofMaxSize(4).uniqueElements();
        return Combinators.combine(id, birthDate, reference, reference, reference, children)
                .as((personId, born, parent1, parent2, partner, kids) ->
                        new Person(personId, null, born, parent1, parent2, partner, kids));
    }
}
