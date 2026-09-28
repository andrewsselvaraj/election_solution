package com.election.solution;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
// Force the key empty even if a local config/application.properties holds a real one
@SpringBootTest(properties = "langchain.openai.api-key=")
class ElectionSolutionApplicationTests {

    @Test
    void contextLoads() {
    }
}
