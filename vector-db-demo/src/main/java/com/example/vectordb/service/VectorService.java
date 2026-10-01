package com.example.vectordb.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * Adds text to the vector store and runs similarity searches against it.
 */
@Service
public class VectorService {

    static final String CATEGORY = "category";

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public VectorService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /** Embeds the text and stores it; returns the generated id. */
    public String add(String text, String category) {
        TextSegment segment = toSegment(text, category);
        Embedding embedding = embeddingModel.embed(segment).content();
        return embeddingStore.add(embedding, segment);
    }

    /** Embeds and stores many documents in one batch; returns the generated ids in order. */
    public List<String> addAll(List<NewDocument> documents) {
        List<TextSegment> segments = documents.stream()
                .map(d -> toSegment(d.text(), d.category()))
                .toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        return embeddingStore.addAll(embeddings, segments);
    }

    /**
     * Finds the documents closest in meaning to the query.
     *
     * @param maxResults how many results to return at most
     * @param minScore   0..1, results with a lower similarity score are dropped
     * @param category   optional metadata filter; null or blank means all categories
     */
    public List<SearchResult> search(String query, int maxResults, double minScore, String category) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        Filter filter = (category == null || category.isBlank()) ? null : metadataKey(CATEGORY).isEqualTo(category);

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .filter(filter)
                .build();

        return embeddingStore.search(request).matches().stream()
                .map(VectorService::toResult)
                .toList();
    }

    /** True when the store holds at least one document. */
    public boolean isEmpty() {
        return search("anything", 1, 0.0, null).isEmpty();
    }

    public void delete(String id) {
        embeddingStore.remove(id);
    }

    public void deleteAll() {
        embeddingStore.removeAll();
    }

    private static TextSegment toSegment(String text, String category) {
        Metadata metadata = new Metadata();
        if (category != null && !category.isBlank()) {
            metadata.put(CATEGORY, category);
        }
        return TextSegment.from(text, metadata);
    }

    private static SearchResult toResult(EmbeddingMatch<TextSegment> match) {
        TextSegment segment = match.embedded();
        return new SearchResult(match.embeddingId(), match.score(), segment.text(), segment.metadata().getString(CATEGORY));
    }

    public record NewDocument(String text, String category) {
    }

    public record SearchResult(String id, double score, String text, String category) {
    }
}
