package nl.kabulter.threekids.acceptance.drivers;

/**
 * How the DSL talks to the service. Implementations speak a protocol; they make no assertions about behaviour.
 */
public interface ServiceDriver {

    String version();
}
