package nl.kabulter.threekids.acceptance.drivers.http;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.time.Duration;

/**
 * The service under test: the image of the current build, started once per test run.
 */
final class ServiceContainer {

    private static final int PORT = 8080;
    private static GenericContainer<?> container;

    private ServiceContainer() {
    }

    static synchronized URI baseUri() {
        if (container == null) {
            container = start();
        }
        return URI.create("http://" + container.getHost() + ":" + container.getMappedPort(PORT));
    }

    private static GenericContainer<?> start() {
        DockerImageName image = DockerImageName.parse(
                System.getProperty("image.name", "three-kids") + ":" + System.getProperty("app.version", "dev"));
        GenericContainer<?> started = new GenericContainer<>(image)
                .withExposedPorts(PORT)
                .waitingFor(Wait.forHttp("/api/version").forStatusCode(200))
                .withStartupTimeout(Duration.ofSeconds(60));
        started.start();
        return started;
    }
}
