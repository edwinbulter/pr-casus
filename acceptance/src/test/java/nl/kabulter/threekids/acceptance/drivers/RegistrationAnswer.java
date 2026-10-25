package nl.kabulter.threekids.acceptance.drivers;

import java.util.List;

/**
 * The answer of the service to a registration: the people that currently match, or nobody yet.
 */
public record RegistrationAnswer(boolean someoneMatches, List<PersonRecord> matchingPeople) {

    public static RegistrationAnswer nobodyMatchesYet() {
        return new RegistrationAnswer(false, List.of());
    }
}
