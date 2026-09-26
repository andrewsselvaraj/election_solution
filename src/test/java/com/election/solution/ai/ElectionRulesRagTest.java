package com.election.solution.ai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionRulesRagTest {

    /** Offline stand-in for OpenAI embeddings: a bag-of-words vector, so similar words = similar vectors. */
    static class WordHashEmbeddingModel implements EmbeddingModel {
        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
            return Response.from(segments.stream().map(s -> toVector(s.text())).toList());
        }

        private static Embedding toVector(String text) {
            float[] vector = new float[256];
            for (String word : text.toLowerCase().split("[^a-z]+")) {
                if (word.length() > 3) {
                    vector[Math.floorMod(word.hashCode(), vector.length)] += 1;
                }
            }
            Embedding embedding = Embedding.from(vector);
            embedding.normalize();
            return embedding;
        }
    }

    private final ContentRetriever retriever = ElectionRulesRag.buildRetriever(new WordHashEmbeddingModel());

    @Test
    void findsTheRuleAboutPhones() {
        List<Content> found = retriever.retrieve(Query.from("Can I take my mobile phone into the voting compartment?"));

        assertThat(found).isNotEmpty();
        assertThat(found.get(0).textSegment().text()).contains("Mobile phones");
    }

    @Test
    void findsTheRuleAboutDocuments() {
        List<Content> found = retriever.retrieve(Query.from("Which identity documents must voters show at the booth?"));

        assertThat(found).isNotEmpty();
        assertThat(found.get(0).textSegment().text()).contains("passport");
    }
}
