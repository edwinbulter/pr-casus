package nl.kabulter.threekids.acceptance.scenarios;

import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

/**
 * Posting a known ID again replaces the old data.
 */
class UpdateScenarios {

    @AutoClose
    private final Registry registry = Registry.empty();

    @Test
    void aCorrectedBirthDateCanEndAMatch() {
        // Given Bart, Lisa and Maggie are registered, each with parents Homer and Marge
        registry.register(person("Bart").bornOn("1999-04-01").withParents("Homer", "Marge"));
        registry.register(person("Lisa").bornOn("2001-05-09").withParents("Homer", "Marge"));
        registry.register(person("Maggie").bornOn("2020-01-12").withParents("Homer", "Marge"));

        // And Marge and Homer are registered as partners with children Bart, Lisa and Maggie
        registry.register(person("Marge").bornOn("1978-03-19")
                .withPartner("Homer")
                .withChildren("Bart", "Lisa", "Maggie"));
        registry.register(person("Homer").bornOn("1976-05-12")
                .withPartner("Marge")
                .withChildren("Bart", "Lisa", "Maggie"));

        // When Maggie is registered again, born 2003-01-12, with parents Homer and Marge
        registry.register(person("Maggie").bornOn("2003-01-12").withParents("Homer", "Marge"));

        // Then the service answers that nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }
}
