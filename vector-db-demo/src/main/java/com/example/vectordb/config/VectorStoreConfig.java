package com.example.vectordb.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the embedding model (text -> vector) and the vector store (where vectors are saved and searched).
 */
@Configuration
public class VectorStoreConfig {

    /** all-MiniLM-L6-v2 runs locally inside the JVM and produces 384-dimension vectors. */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    @ConditionalOnProperty(name = "vector.store", havingValue = "memory", matchIfMissing = true)
    public EmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @ConditionalOnProperty(name = "vector.store", havingValue = "pgvector")
    public EmbeddingStore<TextSegment> pgVectorEmbeddingStore(VectorProperties properties, EmbeddingModel embeddingModel) {
        VectorProperties.PgVector pg = properties.pgvector();
        return PgVectorEmbeddingStore.builder()
                .host(pg.host())
                .port(pg.port())
                .database(pg.database())
                .user(pg.user())
                .password(pg.password())
                .table(pg.table())
                .dimension(embeddingModel.dimension())
                .createTable(true)
                .build();
    }
}
