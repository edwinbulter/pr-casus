package nl.kabulter.threekids.acceptance.dsl;

import nl.kabulter.threekids.acceptance.drivers.PersonRecord;
import nl.kabulter.threekids.acceptance.drivers.RegistrationAnswer;
import nl.kabulter.threekids.acceptance.drivers.ServiceDriver;
import nl.kabulter.threekids.acceptance.drivers.http.HttpServiceDriver;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The people registry as the scenarios see it: register people by alias and check the answer.
 */
public class Registry implements AutoCloseable {

    private final ServiceDriver service;
    private final Map<String, Long> ids = new HashMap<>();
    private RegistrationAnswer lastAnswer;

    private Registry(ServiceDriver service) {
        this.service = service;
    }

    /** A registry on a fresh service, so nobody has been registered yet (ADR 0004). */
    public static Registry empty() {
        return new Registry(HttpServiceDriver.onAFreshService());
    }

    public void register(Person person) {
        lastAnswer = service.register(new PersonRecord(
                idOf(person.alias),
                person.name,
                person.birthDate,
                idOf(person.parent1),
                idOf(person.parent2),
                idOf(person.partner),
                person.children.stream().map(this::idOf).toList()));
    }

    public void answersThatNobodyMatchesYet() {
        assertThat(lastAnswer)
                .as("answer to the last registration")
                .isEqualTo(RegistrationAnswer.nobodyMatchesYet());
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
