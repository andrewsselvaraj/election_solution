package com.election.solution.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the LangChain4j sample. Beans are only created when
 * {@code langchain.openai.api-key} (env {@code OPENAI_API_KEY}) is set.
 */
@Configuration
@ConditionalOnExpression("!'${langchain.openai.api-key:}'.isBlank()")
public class LangChainConfig {

    @Bean
    ChatModel chatModel(@Value("${langchain.openai.api-key}") String apiKey,
                        @Value("${langchain.openai.model-name}") String modelName,
                        @Value("${langchain.openai.max-completion-tokens}") int maxCompletionTokens,
                        @Value("${langchain.openai.log-requests:false}") boolean logRequests) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxCompletionTokens(maxCompletionTokens)
                .logRequests(logRequests)
                .logResponses(logRequests)
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
