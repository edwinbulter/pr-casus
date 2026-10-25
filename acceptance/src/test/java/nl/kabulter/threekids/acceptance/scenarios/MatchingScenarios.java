package nl.kabulter.threekids.acceptance.scenarios;

import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

class MatchingScenarios {

    @AutoClose
    private final Registry registry = Registry.empty();

    @Test
    void aPersonWithoutAThreeKidFamilyDoesNotMatch() {
        // Given nobody has been registered yet (a fresh registry)

        // When Ada is registered as in the casus example
        registry.register(person("Ada")
                .named("Ada Lovelace")
                .bornOn("1815-12-10")
                .withParents("Parent1", "Parent2")
                .withPartner("Partner")
                .withChildren("Child1", "Child2"));

        // Then the service answers that nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }
}
