package nl.kabulter.threekids.api;

import nl.kabulter.threekids.application.PeopleService;
import nl.kabulter.threekids.domain.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/people")
class PeopleController {

    /** "Nobody matches yet": HTTP 444 No Response, without a body. Not in Spring's HttpStatus. */
    private static final int NO_RESPONSE = 444;

    private final PeopleService people;

    PeopleController(PeopleService people) {
        this.people = people;
    }

    @PostMapping
    ResponseEntity<List<Person>> register(@RequestBody PersonJson request) {
        people.register(request.toPerson());
        return ResponseEntity.status(NO_RESPONSE).build();
    }
}
