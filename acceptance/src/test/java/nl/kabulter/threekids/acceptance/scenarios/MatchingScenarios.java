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

    @Test
    void aParentWithAPartnerAndThreeChildrenOneOfThemUnder18Matches() {
        // Given Bart, Lisa and Maggie are registered, each with parents Homer and Marge
        registry.register(person("Bart").bornOn("1999-04-01").withParents("Homer", "Marge"));
        registry.register(person("Lisa").bornOn("2001-05-09").withParents("Homer", "Marge"));
        registry.register(person("Maggie").bornOn("2020-01-12").withParents("Homer", "Marge"));

        // And Marge is registered with partner Homer and children Bart, Lisa and Maggie
        registry.register(person("Marge").bornOn("1978-03-19")
                .withPartner("Homer")
                .withChildren("Bart", "Lisa", "Maggie"));

        // Then Marge matches
        registry.answersThatTheseMatch("Marge");

        // When Homer is registered with partner Marge and children Bart, Lisa and Maggie
        registry.register(person("Homer").bornOn("1976-05-12")
                .withPartner("Marge")
                .withChildren("Bart", "Lisa", "Maggie"));

        // Then the service answers that Homer and Marge match,
        // with everything it knows about them, as they were registered
        registry.answersThatTheseMatch("Homer", "Marge");
    }

    @Test
    void aThreeKidFamilyWithOnlyAdultChildrenDoesNotMatch() {
        // Given Bart, Lisa and Maggie, all adults, are registered, each with parents Homer and Marge
        registry.register(person("Bart").bornOn("1999-04-01").withParents("Homer", "Marge"));
        registry.register(person("Lisa").bornOn("2001-05-09").withParents("Homer", "Marge"));
        registry.register(person("Maggie").bornOn("2003-01-12").withParents("Homer", "Marge"));

        // And Marge is registered with partner Homer and children Bart, Lisa and Maggie
        registry.register(person("Marge").bornOn("1978-03-19")
                .withPartner("Homer")
                .withChildren("Bart", "Lisa", "Maggie"));

        // When Homer is registered with partner Marge and children Bart, Lisa and Maggie
        registry.register(person("Homer").bornOn("1976-05-12")
                .withPartner("Marge")
                .withChildren("Bart", "Lisa", "Maggie"));

        // Then the service answers that nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }

    @Test
    void aThreeKidFamilyWhereOneChildHasAnotherSecondParentDoesNotMatch() {
        // Given Bart and Maggie are registered with parents Homer and Marge
        registry.register(person("Bart").bornOn("1999-04-01").withParents("Homer", "Marge"));
        registry.register(person("Maggie").bornOn("2020-01-12").withParents("Homer", "Marge"));

        // And Lisa is registered with parents Homer and Luann
        registry.register(person("Lisa").bornOn("2001-05-09").withParents("Homer", "Luann"));

        // And Marge is registered with partner Homer and children Bart and Maggie
        registry.register(person("Marge").bornOn("1978-03-19")
                .withPartner("Homer")
                .withChildren("Bart", "Maggie"));

        // When Homer is registered with partner Marge and children Bart, Lisa and Maggie
        registry.register(person("Homer").bornOn("1976-05-12")
                .withPartner("Marge")
                .withChildren("Bart", "Lisa", "Maggie"));

        // Then the service answers that nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }
}
