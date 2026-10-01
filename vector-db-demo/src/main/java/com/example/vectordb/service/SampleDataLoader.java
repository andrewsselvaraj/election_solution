package com.example.vectordb.service;

import com.example.vectordb.config.VectorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads {@code sample-docs.txt} ("category|text" per line) at startup so there is something to search.
 * Skipped when {@code vector.seed-sample-data=false} or the store already contains data.
 */
@Component
public class SampleDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    private final VectorService vectorService;
    private final VectorProperties properties;

    public SampleDataLoader(VectorService vectorService, VectorProperties properties) {
        this.vectorService = vectorService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!properties.seedSampleData()) {
            return;
        }
        if (!vectorService.isEmpty()) {
            log.info("Vector store already has data, skipping sample data");
            return;
        }
        List<VectorService.NewDocument> documents = readSampleDocs();
        vectorService.addAll(documents);
        log.info("Loaded {} sample documents into the '{}' vector store", documents.size(), properties.store());
    }

    static List<VectorService.NewDocument> readSampleDocs() throws IOException {
        String content = new ClassPathResource("sample-docs.txt").getContentAsString(StandardCharsets.UTF_8);
        return content.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .map(line -> line.split("\\|", 2))
                .map(parts -> new VectorService.NewDocument(parts[1].strip(), parts[0].strip()))
                .toList();
    }
}
