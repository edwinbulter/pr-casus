package nl.kabulter.threekids.acceptance.dsl;

import nl.kabulter.threekids.acceptance.drivers.PersonRecord;
import nl.kabulter.threekids.acceptance.drivers.RegistrationAnswer;
import nl.kabulter.threekids.acceptance.drivers.ServiceDriver;
import nl.kabulter.threekids.acceptance.drivers.http.HttpServiceDriver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The people registry as the scenarios see it: register people by alias and check the answer.
 */
public class Registry implements AutoCloseable {

    private final ServiceDriver service;
    private final Map<String, Long> ids = new HashMap<>();
    private final Map<String, PersonRecord> registered = new HashMap<>();
    private RegistrationAnswer lastAnswer;

    private Registry(ServiceDriver service) {
        this.service = service;
    }

    /** A registry on a fresh service, so nobody has been registered yet (ADR 0004). */
    public static Registry empty() {
        return new Registry(HttpServiceDriver.onAFreshService());
    }

    public void register(Person person) {
        PersonRecord record = new PersonRecord(
                idOf(person.alias),
                person.name,
                person.birthDate,
                idOf(person.parent1),
                idOf(person.parent2),
                idOf(person.partner),
                person.children.stream().map(this::idOf).toList());
        registered.put(person.alias, record);
        lastAnswer = service.register(record);
    }

    /** Deletes these people; the service ignores their IDs from now on. */
    public void deletes(String... aliases) {
        service.delete(Arrays.stream(aliases).map(this::idOf).toList());
    }

    /** The service answers that exactly these people match, with everything it knows: here, as they were registered. */
    public void answersThatTheseMatch(String... aliases) {
        answersThatTheseMatch(Arrays.stream(aliases).map(Known::asRegistered).toArray(Known[]::new));
    }

    /** The service answers that exactly these people match, with everything it knows about them. */
    public void answersThatTheseMatch(Known... people) {
        assertThat(lastAnswer.someoneMatches())
                .as("answer to the last registration: does someone match?")
                .isTrue();
        assertThat(lastAnswer.matchingPeople().stream().map(Registry::withSortedChildren).toList())
                .as("matching people")
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(people).map(this::expected).toList());
    }

    public void answersThatNobodyMatchesYet() {
        assertThat(lastAnswer)
                .as("answer to the last registration")
                .isEqualTo(RegistrationAnswer.nobodyMatchesYet());
    }

    private PersonRecord expected(Known known) {
        PersonRecord record = registered.get(known.alias);
        if (known.children != null) {
            record = new PersonRecord(record.id(), record.name(), record.birthDate(),
                    record.parent1(), record.parent2(), record.partner(),
                    known.children.stream().map(this::idOf).toList());
        }
        return withSortedChildren(record);
    }

    /** The order of the children is up to the service. */
    private static PersonRecord withSortedChildren(PersonRecord record) {
        return new PersonRecord(record.id(), record.name(), record.birthDate(),
                record.parent1(), record.parent2(), record.partner(),
                record.children().stream().sorted().toList());
    }

    @Override
    public void close() {
        service.close();
    }

    private Long idOf(String alias) {
        if (alias == null) {
            return null;
        }
        return ids.computeIfAbsent(alias, unused -> (long) ids.size() + 1);
    }
}
