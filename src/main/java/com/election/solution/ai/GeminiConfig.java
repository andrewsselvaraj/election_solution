package com.election.solution.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the Gemini assistant. Only active when {@code langchain.gemini.api-key}
 * (env {@code GEMINI_API_KEY}) is set.
 */
@Configuration
@ConditionalOnExpression("!'${langchain.gemini.api-key:}'.isBlank()")
public class GeminiConfig {

    @Bean
    GeminiAssistant geminiAssistant(@Value("${langchain.gemini.api-key}") String apiKey,
                                    @Value("${langchain.gemini.model-name}") String modelName,
                                    @Value("${langchain.gemini.max-output-tokens}") int maxOutputTokens,
                                    @Value("${langchain.gemini.log-requests:false}") boolean logRequests) {
        // Built here rather than as a ChatModel bean, so it does not clash with the OpenAI ChatModel
        GoogleAiGeminiChatModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxOutputTokens(maxOutputTokens)
                .logRequestsAndResponses(logRequests)
                .build();

        return AiServices.builder(GeminiAssistant.class)
                .chatModel(gemini)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}
