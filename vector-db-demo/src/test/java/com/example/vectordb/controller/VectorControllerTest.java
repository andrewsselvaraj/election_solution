package com.example.vectordb.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full application (in-memory store + real embedding model) driven through the REST API.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class VectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addThenSearchThenDelete() throws Exception {
        mockMvc.perform(delete("/api/documents")).andExpect(status().isNoContent());

        mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Elephants are the largest land mammals.\",\"category\":\"animals\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
        mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Spring Boot creates stand-alone Java applications.\",\"category\":\"tech\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/search").param("q", "biggest animal on land").param("k", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text", containsString("Elephants")))
                .andExpect(jsonPath("$[0].category").value("animals"));

        mockMvc.perform(delete("/api/documents")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/search").param("q", "biggest animal on land"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void blankTextIsRejected() throws Exception {
        mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
