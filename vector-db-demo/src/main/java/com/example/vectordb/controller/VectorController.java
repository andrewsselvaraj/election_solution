package com.example.vectordb.controller;

import com.example.vectordb.service.VectorService;
import com.example.vectordb.service.VectorService.SearchResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for adding documents to the vector store and searching it.
 */
@Validated
@RestController
@RequestMapping("/api")
public class VectorController {

    private final VectorService vectorService;

    public VectorController(VectorService vectorService) {
        this.vectorService = vectorService;
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> add(@Valid @RequestBody AddDocumentRequest request) {
        return Map.of("id", vectorService.add(request.text(), request.category()));
    }

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam("q") @NotBlank String query,
                                     @RequestParam(defaultValue = "3") @Min(1) @Max(50) int k,
                                     @RequestParam(defaultValue = "0.0") @Min(0) @Max(1) double minScore,
                                     @RequestParam(required = false) String category) {
        return vectorService.search(query, k, minScore, category);
    }

    @DeleteMapping("/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        vectorService.delete(id);
    }

    @DeleteMapping("/documents")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        vectorService.deleteAll();
    }

    public record AddDocumentRequest(@NotBlank String text, String category) {
    }
}
