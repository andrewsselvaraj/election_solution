package com.election.solution.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.List;

/**
 * RAG (retrieval-augmented generation) over the documents in {@code classpath:rag/}.
 * <ol>
 *   <li>Load the documents and split them into small chunks.</li>
 *   <li>Turn each chunk into an embedding (a vector of numbers capturing its meaning).</li>
 *   <li>At question time, find the chunks closest in meaning to the question;
 *       LangChain4j adds them to the prompt so GPT answers from them.</li>
 * </ol>
 */
public final class ElectionRulesRag {

    private ElectionRulesRag() {
    }

    public static ContentRetriever buildRetriever(EmbeddingModel embeddingModel) {
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("rag", new TextDocumentParser());

        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build()
                .ingest(documents);

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();
    }
}
