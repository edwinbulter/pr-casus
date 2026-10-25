package nl.kabulter.threekids.application;

import nl.kabulter.threekids.domain.People;
import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.store.InMemoryPeople;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * The real store, counting how often the children of a person are asked: once for every candidate the pattern
 * evaluates.
 */
class CountingPeople implements People {

    private final InMemoryPeople people = new InMemoryPeople();
    private int childrenAsked;

    int childrenAsked() {
        return childrenAsked;
    }

    void resetCount() {
        childrenAsked = 0;
    }

    @Override
    public void save(Person person) {
        people.save(person);
    }

    @Override
    public void delete(long id) {
        people.delete(id);
    }

    @Override
    public Collection<Person> all() {
        return people.all();
    }

    @Override
    public Optional<Person> find(long id) {
        return people.find(id);
    }

    @Override
    public Set<Long> parentsOf(long child) {
        return people.parentsOf(child);
    }

    @Override
    public Set<Long> childrenOf(long parent) {
        childrenAsked++;
        return people.childrenOf(parent);
    }
}
