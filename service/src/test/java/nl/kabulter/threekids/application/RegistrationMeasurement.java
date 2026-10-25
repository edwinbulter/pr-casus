package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.store.InMemoryPeople;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

/**
 * A measurement, not a test: the time per registration for a growing number of people, in-process (PeopleService
 * with the in-memory store, without HTTP). Run with make perf; sizes with -Dperf.sizes=10000,100000.
 * <p>
 * The population is made of families of five: two partners and three children. Ten families have a child under 18
 * and match; in all others the children are adults, so they are candidates that do not match.
 */
@Tag("perf")
class RegistrationMeasurement {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC);
    private static final int MATCHING_FAMILIES = 10;

    @Test
    void timePerRegistration() {
        int[] sizes = Arrays.stream(System.getProperty("perf.sizes", "10000,20000,100000").split(","))
                .mapToInt(Integer::parseInt).toArray();
        System.out.println("| People registered | Total time | Average time per registration | Matching at the end |");
        System.out.println("| ---: | ---: | ---: | ---: |");
        for (int size : sizes) {
            measure(size);
        }
    }

    private static void measure(int size) {
        PeopleService people = new PeopleService(new InMemoryPeople(), CLOCK);
        List<Person> registrations = families(size / 5);
        long start = System.nanoTime();
        List<Person> lastAnswer = List.of();
        for (Person person : registrations) {
            lastAnswer = people.register(person);
        }
        long nanos = System.nanoTime() - start;
        System.out.printf("| %,d | %,d ms | %,.1f µs | %,d |%n",
                registrations.size(), nanos / 1_000_000, nanos / 1_000.0 / registrations.size(), lastAnswer.size());
    }

    private static List<Person> families(int count) {
        LocalDate adult = LocalDate.of(1990, 1, 1);
        LocalDate minor = LocalDate.of(2015, 1, 1);
        return java.util.stream.IntStream.range(0, count).boxed().flatMap(family -> {
            long first = family * 5L + 1;
            long parent = first, partner = first + 1, a = first + 2, b = first + 3, c = first + 4;
            LocalDate youngest = family < MATCHING_FAMILIES ? minor : adult;
            return java.util.stream.Stream.of(
                    new Person(a, null, adult, parent, partner, null, List.of()),
                    new Person(b, null, adult, parent, partner, null, List.of()),
                    new Person(c, null, youngest, parent, partner, null, List.of()),
                    new Person(parent, null, adult, null, null, partner, List.of(a, b, c)),
                    new Person(partner, null, adult, null, null, parent, List.of(a, b, c)));
        }).toList();
    }
}
