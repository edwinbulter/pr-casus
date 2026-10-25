package nl.kabulter.threekids.domain;

/**
 * The pattern the service looks for: a person with a partner and exactly three children.
 */
public final class ThreeKidFamily {

    private ThreeKidFamily() {
    }

    public static boolean isMatchedBy(Person person) {
        return person.partner() != null && person.children().size() == 3;
    }
}
