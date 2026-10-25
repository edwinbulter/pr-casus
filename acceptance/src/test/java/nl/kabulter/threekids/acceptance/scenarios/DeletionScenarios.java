package nl.kabulter.threekids.acceptance.scenarios;

import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

/**
 * Phase 2: deleted IDs are removed and ignored forever, for storage and for matching.
 */
class DeletionScenarios {

    @AutoClose
    private final Registry registry = Registry.empty();

    @Test
    void aDeletedPersonNoLongerCountsForMatching() {
        // Given Bart, Lisa and Maggie are registered, each with parents Homer and Marge
        registry.register(person("Bart").bornOn("1999-04-01").withParents("Homer", "Marge"));
        registry.register(person("Lisa").bornOn("2001-05-09").withParents("Homer", "Marge"));
        registry.register(person("Maggie").bornOn("2020-01-12").withParents("Homer", "Marge"));

        // And Marge is registered with partner Homer and children Bart, Lisa and Maggie
        registry.register(person("Marge").bornOn("1978-03-19")
                .withPartner("Homer")
                .withChildren("Bart", "Lisa", "Maggie"));

        // When Maggie is deleted
        registry.deletes("Maggie");

        // And Homer is registered with partner Marge and children Bart, Lisa and Maggie
        registry.register(person("Homer").bornOn("1976-05-12")
                .withPartner("Marge")
                .withChildren("Bart", "Lisa", "Maggie"));

        // Then the service answers that nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }

    @Test
    void aDeletedPersonWhoIsRegisteredAgainIsIgnored() {
        // Given Bart, Lisa and Maggie are registered, each with parents Homer and Marge
        registry.register(person("Bart").bornOn("1999-04-01").withParents("Homer", "Marge"));
        registry.register(person("Lisa").bornOn("2001-05-09").withParents("Homer", "Marge"));
        registry.register(person("Maggie").bornOn("2020-01-12").withParents("Homer", "Marge"));

        // And Marge is registered with partner Homer and children Bart, Lisa and Maggie
        registry.register(person("Marge").bornOn("1978-03-19")
                .withPartner("Homer")
                .withChildren("Bart", "Lisa", "Maggie"));

        // And Maggie is deleted
        registry.deletes("Maggie");

        // When Maggie is registered again, born 2020-01-12, with parents Homer and Marge
        registry.register(person("Maggie").bornOn("2020-01-12").withParents("Homer", "Marge"));

        // And Homer is registered with partner Marge and children Bart, Lisa and Maggie
        registry.register(person("Homer").bornOn("1976-05-12")
                .withPartner("Marge")
                .withChildren("Bart", "Lisa", "Maggie"));

        // Then the service answers that nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }
}
