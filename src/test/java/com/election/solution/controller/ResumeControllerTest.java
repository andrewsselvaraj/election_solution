package com.election.solution.controller;

import com.election.solution.ai.ResumeAssistant;
import com.election.solution.resume.ResumeTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ResumeController.class)
@Import(ResumeTextExtractor.class)
class ResumeControllerTest {

    private static final String RESUME = "Jane Doe\nSkills: Java, Spring Boot, LangChain4j";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeAssistant assistant;

    @Test
    void uploadThenAskSendsResumeTextToAssistant() throws Exception {
        MockHttpSession session = new MockHttpSession();
        given(assistant.ask(anyString(), eq(RESUME), eq("What are my skills?")))
                .willReturn("Java, Spring Boot and LangChain4j.");

        mockMvc.perform(multipart("/resume/upload").file(txt(RESUME)).session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Resume loaded. Ask your question below."));

        mockMvc.perform(post("/resume/ask").param("question", "What are my skills?").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Java, Spring Boot and LangChain4j.")));
    }

    @Test
    void askingBeforeUploadShowsError() throws Exception {
        mockMvc.perform(post("/resume/ask").param("question", "What are my skills?"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Upload your resume first."));

        verifyNoInteractions(assistant);
    }

    @Test
    void unsupportedFileShowsError() throws Exception {
        var docx = new MockMultipartFile("file", "resume.docx", "application/octet-stream", new byte[]{1});

        mockMvc.perform(multipart("/resume/upload").file(docx))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Only .pdf, .txt or .md resumes are supported."));
    }

    private static MockMultipartFile txt(String text) {
        return new MockMultipartFile("file", "resume.txt", "text/plain", text.getBytes(StandardCharsets.UTF_8));
    }
}
