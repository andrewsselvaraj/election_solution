package com.election.solution.controller;

import com.election.solution.ai.ElectionAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@WebMvcTest(AssistantController.class)
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElectionAssistant assistant;

    @Test
    void formRendersWhenAssistantConfigured() throws Exception {
        mockMvc.perform(get("/assistant"))
                .andExpect(status().isOk())
                .andExpect(view().name("assistant"))
                .andExpect(model().attribute("enabled", true));
    }

    @Test
    void questionIsAnsweredByAssistant() throws Exception {
        given(assistant.chat("When is polling day?")).willReturn("Polling day is 2026-11-03.");

        mockMvc.perform(post("/assistant").param("question", "When is polling day?"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("answer", "Polling day is 2026-11-03."))
                .andExpect(content().string(containsString("Polling day is 2026-11-03.")));
    }
}
