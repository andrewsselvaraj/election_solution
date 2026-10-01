package com.example.vectordb;

import com.example.vectordb.service.VectorService;
import com.example.vectordb.service.VectorService.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the app against a real PostgreSQL + pgvector started by Testcontainers.
 * Skipped automatically when Docker is not available.
 */
@ActiveProfiles({"test", "pgvector"})
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PgVectorStoreTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void pgvectorProperties(DynamicPropertyRegistry registry) {
        registry.add("vector.pgvector.host", postgres::getHost);
        registry.add("vector.pgvector.port", () -> postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
        registry.add("vector.pgvector.database", postgres::getDatabaseName);
        registry.add("vector.pgvector.user", postgres::getUsername);
        registry.add("vector.pgvector.password", postgres::getPassword);
    }

    @Autowired
    private VectorService vectorService;

    @Test
    void storesAndSearchesInPostgres() {
        vectorService.deleteAll();
        List<String> ids = vectorService.addAll(List.of(
                new VectorService.NewDocument("The cheetah is the fastest land animal.", "animals"),
                new VectorService.NewDocument("Sourdough bread is made with wild yeast.", "food"),
                new VectorService.NewDocument("Penguins are flightless birds.", "animals")));
        assertThat(ids).hasSize(3);

        List<SearchResult> results = vectorService.search("quickest runner", 1, 0.0, null);
        assertThat(results).singleElement().satisfies(r -> assertThat(r.text()).contains("cheetah"));

        List<SearchResult> food = vectorService.search("birds", 3, 0.0, "food");
        assertThat(food).singleElement().satisfies(r -> assertThat(r.category()).isEqualTo("food"));

        vectorService.delete(ids.getFirst());
        assertThat(vectorService.search("quickest runner", 3, 0.0, null))
                .extracting(SearchResult::id).doesNotContain(ids.getFirst());
    }
}
