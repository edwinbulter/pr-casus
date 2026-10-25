package nl.kabulter.threekids.acceptance.scenarios;

import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

import static nl.kabulter.threekids.acceptance.dsl.Known.asRegistered;
import static nl.kabulter.threekids.acceptance.dsl.Person.person;

/**
 * Bidirectional integrity: if A says B is a child, B lists A as a parent, and the other way round.
 */
class IntegrityScenarios {

    @AutoClose
    private final Registry registry = Registry.empty();

    @Test
    void childrenWhoDoNotNameTheirParentsStillCountWhenTheParentsNameThem() {
        // Given Bart, Lisa and Maggie are registered without parents
        registry.register(person("Bart").bornOn("1999-04-01"));
        registry.register(person("Lisa").bornOn("2001-05-09"));
        registry.register(person("Maggie").bornOn("2020-01-12"));

        // And Marge is registered with partner Homer and children Bart, Lisa and Maggie
        registry.register(person("Marge").bornOn("1978-03-19")
                .withPartner("Homer")
                .withChildren("Bart", "Lisa", "Maggie"));

        // When Homer is registered with partner Marge and children Bart, Lisa and Maggie
        registry.register(person("Homer").bornOn("1976-05-12")
                .withPartner("Marge")
                .withChildren("Bart", "Lisa", "Maggie"));

        // Then the service answers that Homer and Marge match,
        // with everything it knows about them, as they were registered
        registry.answersThatTheseMatch("Homer", "Marge");
    }

    @Test
    void parentsWhoDoNotNameTheirChildrenStillMatchWhenTheChildrenNameThem() {
        // Given Bart, Lisa and Maggie are registered, each with parents Homer and Marge
        registry.register(person("Bart").bornOn("1999-04-01").withParents("Homer", "Marge"));
        registry.register(person("Lisa").bornOn("2001-05-09").withParents("Homer", "Marge"));
        registry.register(person("Maggie").bornOn("2020-01-12").withParents("Homer", "Marge"));

        // And Marge is registered with partner Homer, without children
        registry.register(person("Marge").bornOn("1978-03-19").withPartner("Homer"));

        // Then Marge matches, known with children Bart, Lisa and Maggie
        registry.answersThatTheseMatch(asRegistered("Marge").withChildren("Bart", "Lisa", "Maggie"));

        // When Homer is registered with partner Marge, without children
        registry.register(person("Homer").bornOn("1976-05-12").withPartner("Marge"));

        // Then the service answers that Homer and Marge match,
        // each as registered, with children Bart, Lisa and Maggie
        registry.answersThatTheseMatch(
                asRegistered("Homer").withChildren("Bart", "Lisa", "Maggie"),
                asRegistered("Marge").withChildren("Bart", "Lisa", "Maggie"));
    }
}
