package com.example.vectordb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings under {@code vector.*} in application.properties.
 *
 * @param store            "memory" or "pgvector"
 * @param seedSampleData   load sample-docs.txt at startup when the store is empty
 * @param pgvector         connection settings used when {@code store=pgvector}
 */
@ConfigurationProperties("vector")
public record VectorProperties(
        @DefaultValue("memory") String store,
        @DefaultValue("true") boolean seedSampleData,
        @DefaultValue PgVector pgvector) {

    public record PgVector(
            @DefaultValue("localhost") String host,
            @DefaultValue("5432") int port,
            @DefaultValue("vectordb") String database,
            @DefaultValue("vector") String user,
            @DefaultValue("vector") String password,
            @DefaultValue("documents") String table) {
    }
}
