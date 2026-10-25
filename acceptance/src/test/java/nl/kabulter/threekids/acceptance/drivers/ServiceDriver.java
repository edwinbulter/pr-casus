package nl.kabulter.threekids.acceptance.drivers;

/**
 * How the DSL talks to the service. Implementations speak a protocol; they make no assertions about behaviour.
 * Each driver has its own fresh service; closing the driver stops it (ADR 0004).
 */
public interface ServiceDriver extends AutoCloseable {

    String version();

    RegistrationAnswer register(PersonRecord person);

    @Override
    void close();
}
