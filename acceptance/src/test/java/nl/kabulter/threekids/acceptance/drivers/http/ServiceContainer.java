package nl.kabulter.threekids.acceptance.drivers.http;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.time.Duration;

/**
 * A fresh service under test: the image of the current build, in its own container (ADR 0004).
 */
final class ServiceContainer implements AutoCloseable {

    private static final int PORT = 8080;

    private final GenericContainer<?> container;

    private ServiceContainer(GenericContainer<?> container) {
        this.container = container;
    }

    static ServiceContainer start() {
        DockerImageName image = DockerImageName.parse(
                System.getProperty("image.name", "three-kids") + ":" + System.getProperty("app.version", "dev"));
        GenericContainer<?> container = new GenericContainer<>(image)
                .withExposedPorts(PORT)
                .waitingFor(Wait.forHttp("/api/version").forStatusCode(200))
                .withStartupTimeout(Duration.ofSeconds(60));
        container.start();
        return new ServiceContainer(container);
    }

    URI baseUri() {
        return URI.create("http://" + container.getHost() + ":" + container.getMappedPort(PORT));
    }

    @Override
    public void close() {
        container.stop();
    }
}
