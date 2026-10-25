package nl.kabulter.threekids.acceptance.dsl;

import java.util.List;

/**
 * What the service should know about a person: the record as registered, with the relations the service derived.
 */
public final class Known {

    final String alias;
    List<String> children;

    private Known(String alias) {
        this.alias = alias;
    }

    /** The person as registered under this alias. */
    public static Known asRegistered(String alias) {
        return new Known(alias);
    }

    /** ... but with these children, also those the person did not name itself. */
    public Known withChildren(String... children) {
        this.children = List.of(children);
        return this;
    }
}
