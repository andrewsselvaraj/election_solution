package com.example.vectordb.service;

import com.example.vectordb.service.VectorService.NewDocument;
import com.example.vectordb.service.VectorService.SearchResult;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses the real local embedding model with an in-memory store, so similarity results are real.
 */
class VectorServiceTest {

    private static EmbeddingModel embeddingModel;

    private VectorService service;

    @BeforeAll
    static void loadModel() {
        embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }

    @BeforeEach
    void setUp() {
        service = new VectorService(embeddingModel, new InMemoryEmbeddingStore<TextSegment>());
        service.addAll(List.of(
                new NewDocument("The cheetah is the fastest land animal.", "animals"),
                new NewDocument("Sushi is a Japanese dish of vinegared rice and raw fish.", "food"),
                new NewDocument("pgvector adds vector similarity search to PostgreSQL.", "tech")));
    }

    @Test
    void findsDocumentByMeaningNotExactWords() {
        List<SearchResult> results = service.search("Which creature runs quickest?", 1, 0.0, null);

        assertThat(results).singleElement()
                .satisfies(r -> assertThat(r.text()).contains("cheetah"))
                .satisfies(r -> assertThat(r.category()).isEqualTo("animals"));
    }

    @Test
    void resultsAreOrderedByScore() {
        List<SearchResult> results = service.search("database for embeddings", 3, 0.0, null);

        assertThat(results).hasSize(3);
        assertThat(results.getFirst().category()).isEqualTo("tech");
        assertThat(results).extracting(SearchResult::score).isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void categoryFilterRestrictsResults() {
        List<SearchResult> results = service.search("fastest land animal", 3, 0.0, "food");

        assertThat(results).isNotEmpty().allSatisfy(r -> assertThat(r.category()).isEqualTo("food"));
    }

    @Test
    void minScoreDropsWeakMatches() {
        assertThat(service.search("fastest land animal", 3, 0.95, null)).isEmpty();
    }

    @Test
    void deleteRemovesDocument() {
        String id = service.add("Mars is called the Red Planet.", "space");
        assertThat(service.search("red planet", 1, 0.0, null)).extracting(SearchResult::id).containsExactly(id);

        service.delete(id);

        assertThat(service.search("red planet", 3, 0.0, null)).extracting(SearchResult::id).doesNotContain(id);
    }

    @Test
    void deleteAllEmptiesStore() {
        assertThat(service.isEmpty()).isFalse();

        service.deleteAll();

        assertThat(service.isEmpty()).isTrue();
    }

    @Test
    void sampleDocsAreParsed() throws Exception {
        List<NewDocument> docs = SampleDataLoader.readSampleDocs();

        assertThat(docs).isNotEmpty().allSatisfy(d -> {
            assertThat(d.text()).isNotBlank();
            assertThat(d.category()).isNotBlank();
        });
    }
}
