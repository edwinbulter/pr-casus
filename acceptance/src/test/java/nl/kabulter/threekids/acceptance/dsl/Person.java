package nl.kabulter.threekids.acceptance.dsl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A person record in the words of the scenario. People are named by alias; the {@link Registry} gives them IDs.
 */
public final class Person {

    final String alias;
    String name;
    LocalDate birthDate;
    String parent1;
    String parent2;
    String partner;
    final List<String> children = new ArrayList<>();

    private Person(String alias) {
        this.alias = alias;
    }

    public static Person person(String alias) {
        return new Person(alias);
    }

    public Person named(String name) {
        this.name = name;
        return this;
    }

    public Person bornOn(String isoDate) {
        this.birthDate = LocalDate.parse(isoDate);
        return this;
    }

    public Person withParents(String parent1, String parent2) {
        this.parent1 = parent1;
        this.parent2 = parent2;
        return this;
    }

    public Person withPartner(String partner) {
        this.partner = partner;
        return this;
    }

    public Person withChildren(String... children) {
        this.children.addAll(List.of(children));
        return this;
    }
}
