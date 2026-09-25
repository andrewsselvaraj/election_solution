package com.election.solution.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the LangChain4j sample. Beans are only created when
 * {@code langchain.anthropic.api-key} (env {@code ANTHROPIC_API_KEY}) is set.
 */
@Configuration
@ConditionalOnExpression("!'${langchain.anthropic.api-key:}'.isBlank()")
public class LangChainConfig {

    @Bean
    ChatModel chatModel(@Value("${langchain.anthropic.api-key}") String apiKey,
                        @Value("${langchain.anthropic.model-name}") String modelName,
                        @Value("${langchain.anthropic.max-tokens}") int maxTokens) {
        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .build();
    }

    @Bean
    ElectionAssistant electionAssistant(ChatModel chatModel, ElectionTools tools) {
        return AiServices.builder(ElectionAssistant.class)
                .chatModel(chatModel)
                .tools(tools)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}
