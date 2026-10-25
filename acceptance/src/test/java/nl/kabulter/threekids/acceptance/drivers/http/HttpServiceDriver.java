package nl.kabulter.threekids.acceptance.drivers.http;

import nl.kabulter.threekids.acceptance.drivers.ServiceDriver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Talks to a fresh service over its public HTTP API.
 */
public class HttpServiceDriver implements ServiceDriver {

    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private final JsonMapper json = JsonMapper.builder().build();
    private final ServiceContainer service;
    private final URI baseUri;

    private HttpServiceDriver(ServiceContainer service) {
        this.service = service;
        this.baseUri = service.baseUri();
    }

    public static HttpServiceDriver onAFreshService() {
        return new HttpServiceDriver(ServiceContainer.start());
    }

    @Override
    public String version() {
        HttpResponse<String> response = send(HttpRequest.newBuilder(baseUri.resolve("/api/version")).GET().build());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("GET /api/version returned " + response.statusCode() + ": " + response.body());
        }
        JsonNode body = json.readTree(response.body());
        return body.path("version").asString();
    }

    @Override
    public void close() {
        service.close();
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
