package com.election.solution.service;

import com.election.solution.ai.GeminiAssistant;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/** Business layer between the web controller and the Gemini AI service. */
@Service
public class GeminiChatService {

    private final ObjectProvider<GeminiAssistant> assistant;

    public GeminiChatService(ObjectProvider<GeminiAssistant> assistant) {
        this.assistant = assistant;
    }

    /** False when no GEMINI_API_KEY is configured. */
    public boolean isEnabled() {
        return assistant.getIfAvailable() != null;
    }

    public String ask(String conversationId, String message) {
        GeminiAssistant ai = assistant.getIfAvailable();
        if (ai == null) {
            throw new IllegalStateException("Gemini is not configured. Set GEMINI_API_KEY and restart.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Please type a question.");
        }
        try {
            return ai.chat(conversationId, message.strip());
        } catch (RuntimeException e) {
            // wrong key, quota exceeded, network down... show a message instead of an error page
            throw new IllegalStateException("Gemini request failed: " + e.getMessage(), e);
        }
    }
}
