package com.election.solution.resume;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Turns an uploaded resume (PDF or plain text) into text for the AI. */
@Component
public class ResumeTextExtractor {

    /** Caps the prompt size (and cost); a typical resume is well under this. */
    static final int MAX_CHARS = 20_000;

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a resume file to upload.");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            String text;
            if (name.endsWith(".pdf")) {
                text = pdfText(file.getBytes());
            } else if (name.endsWith(".txt") || name.endsWith(".md")) {
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("Only .pdf, .txt or .md resumes are supported.");
            }
            text = text.strip();
            if (text.isEmpty()) {
                throw new IllegalArgumentException("No text found in the resume (a scanned image PDF has no text).");
            }
            return text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the resume file: " + e.getMessage(), e);
        }
    }

    private static String pdfText(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
