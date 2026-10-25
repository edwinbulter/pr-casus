package nl.kabulter.threekids.acceptance.drivers.http;

import nl.kabulter.threekids.acceptance.drivers.PersonRecord;
import nl.kabulter.threekids.acceptance.drivers.RegistrationAnswer;
import nl.kabulter.threekids.acceptance.drivers.ServiceDriver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

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

    /** The casus answers "nobody matches yet" with HTTP 444 No Response. */
    private static final int NO_RESPONSE = 444;

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
    public RegistrationAnswer register(PersonRecord person) {
        HttpResponse<String> response = send(HttpRequest.newBuilder(baseUri.resolve("/api/v1/people"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(toJson(person))))
                .build());
        if (response.statusCode() == NO_RESPONSE) {
            return RegistrationAnswer.nobodyMatchesYet();
        }
        throw new IllegalStateException("POST /api/v1/people returned " + response.statusCode() + ": " + response.body());
    }

    @Override
    public void close() {
        service.close();
    }

    private ObjectNode toJson(PersonRecord person) {
        ObjectNode node = json.createObjectNode();
        node.put("id", person.id());
        if (person.name() != null) {
            node.put("name", person.name());
        }
        if (person.birthDate() != null) {
            node.put("birthDate", person.birthDate().toString());
        }
        putReference(node, "parent1", person.parent1());
        putReference(node, "parent2", person.parent2());
        putReference(node, "partner", person.partner());
        ArrayNode children = node.putArray("children");
        person.children().forEach(child -> children.addObject().put("id", child));
        return node;
    }

    private static void putReference(ObjectNode node, String field, Long id) {
        if (id != null) {
            node.putObject(field).put("id", id);
        }
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
