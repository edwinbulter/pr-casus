package nl.kabulter.threekids.api;

import nl.kabulter.threekids.application.PeopleService;
import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PeopleController.class)
class PeopleControllerTests {

    private static final String ADA = """
            {
              "id"       :  42,
              "name"     : "Ada Lovelace",
              "birthDate": "1815-12-10",
              "parent1"  : { "id": 1 },
              "parent2"  : { "id": 2 },
              "partner"  : { "id": 99 },
              "children" : [{ "id": 77 }, { "id": 78 }]
            }
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PeopleService people;

    @Test
    void registersThePersonFromTheRequest() throws Exception {
        when(people.register(any())).thenReturn(List.of());

        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(ADA));

        verify(people).register(new Person(42, "Ada Lovelace", LocalDate.of(1815, 12, 10), 1L, 2L, 99L, List.of(77L, 78L)));
    }

    @Test
    void answers444WithoutABodyWhenNobodyMatchesYet() throws Exception {
        when(people.register(any())).thenReturn(List.of());

        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(ADA))
                .andExpect(status().is(444))
                .andExpect(content().string(""));
    }
}
