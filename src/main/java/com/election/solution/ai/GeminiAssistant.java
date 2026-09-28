package com.election.solution.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * General chat assistant backed by Google Gemini. Same LangChain4j AI-service idea as
 * {@link ElectionAssistant}; only the chat model behind it is different.
 */
public interface GeminiAssistant {

    @SystemMessage("You are a helpful assistant. Keep answers clear and concise.")
    String chat(@MemoryId String conversationId, @UserMessage String message);
}
