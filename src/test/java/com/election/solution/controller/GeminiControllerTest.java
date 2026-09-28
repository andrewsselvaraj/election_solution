package com.election.solution.controller;

import com.election.solution.ai.GeminiAssistant;
import com.election.solution.service.GeminiChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@WebMvcTest(GeminiController.class)
@Import(GeminiChatService.class)
class GeminiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeminiAssistant assistant;

    @Test
    void pageRendersWhenGeminiConfigured() throws Exception {
        mockMvc.perform(get("/gemini"))
                .andExpect(status().isOk())
                .andExpect(view().name("gemini"))
                .andExpect(model().attribute("enabled", true));
    }

    @Test
    void questionIsAnsweredByGemini() throws Exception {
        MockHttpSession session = new MockHttpSession(null, "s1");
        given(assistant.chat("s1", "Explain how AI works in a few words"))
                .willReturn("AI learns patterns from data.");

        mockMvc.perform(post("/gemini").session(session).param("question", "Explain how AI works in a few words"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AI learns patterns from data.")));
    }
}
