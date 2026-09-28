package com.election.solution.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Wires the LangChain4j sample. Beans are only created when
 * {@code langchain.openai.api-key} (env {@code OPENAI_API_KEY}) is set.
 */
@Configuration
@ConditionalOnExpression("!'${langchain.openai.api-key:}'.isBlank()")
public class LangChainConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChainConfig.class);

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
    EmbeddingModel embeddingModel(@Value("${langchain.openai.api-key}") String apiKey,
                                  @Value("${langchain.openai.embedding-model-name}") String modelName) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    ContentRetriever electionRulesRetriever(EmbeddingModel embeddingModel) {
        try {
            return ElectionRulesRag.buildRetriever(embeddingModel);
        } catch (RuntimeException e) {
            // e.g. invalid key or no network: keep the app running, just without RAG
            log.warn("Could not index election rules for RAG; continuing without them: {}", e.getMessage());
            return query -> List.of();
        }
    }

    @Bean
    ElectionAssistant electionAssistant(ChatModel chatModel, ElectionTools tools, ContentRetriever electionRulesRetriever) {
        return AiServices.builder(ElectionAssistant.class)
                .chatModel(chatModel)
                .tools(tools)
                // RAG: relevant chunks of the election rules are added to each question
                .contentRetriever(electionRulesRetriever)
                // one memory per conversation id, keeping the last 20 messages
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    @Bean
    ResumeAssistant resumeAssistant(ChatModel chatModel) {
        return AiServices.builder(ResumeAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
