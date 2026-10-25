package nl.kabulter.threekids.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reports which build is running, so an acceptance test can check that it tests what was just built.
 */
@RestController
class VersionController {

    private final String version;

    VersionController(@Value("${app.version}") String version) {
        this.version = version;
    }

    @GetMapping("/api/version")
    VersionResponse version() {
        return new VersionResponse(version);
    }

    record VersionResponse(String version) {
    }
}
