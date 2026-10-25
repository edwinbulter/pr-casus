package nl.kabulter.threekids;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ThreeKidsApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void withoutABuildVersionTheServiceReportsDev() throws Exception {
        mvc.perform(get("/api/version"))
                .andExpect(jsonPath("$.version").value("dev"));
    }
}
