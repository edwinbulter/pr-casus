package nl.kabulter.threekids.acceptance.dsl;

import nl.kabulter.threekids.acceptance.drivers.ServiceDriver;
import nl.kabulter.threekids.acceptance.drivers.http.HttpServiceDriver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Someone who runs the service and wants to know which build is running.
 */
public class Operator implements AutoCloseable {

    private final ServiceDriver service;
    private String reportedVersion;

    public Operator() {
        this(HttpServiceDriver.onAFreshService());
    }

    Operator(ServiceDriver service) {
        this.service = service;
    }

    public void asksForTheVersion() {
        reportedVersion = service.version();
    }

    public void seesVersion(String expected) {
        assertThat(reportedVersion)
                .as("version reported by the service")
                .isEqualTo(expected);
    }

    @Override
    public void close() {
        service.close();
    }
}
