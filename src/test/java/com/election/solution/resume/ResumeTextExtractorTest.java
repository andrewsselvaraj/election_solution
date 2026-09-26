package com.election.solution.resume;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeTextExtractorTest {

    private final ResumeTextExtractor extractor = new ResumeTextExtractor();

    @Test
    void readsPlainTextResume() {
        var file = new MockMultipartFile("file", "resume.txt", "text/plain",
                "Jane Doe\nSkills: Java, Spring Boot".getBytes(StandardCharsets.UTF_8));

        assertThat(extractor.extract(file)).isEqualTo("Jane Doe\nSkills: Java, Spring Boot");
    }

    @Test
    void readsPdfResume() throws Exception {
        var file = new MockMultipartFile("file", "Resume.PDF", "application/pdf", pdfWith("Jane Doe - Java Developer"));

        assertThat(extractor.extract(file)).contains("Jane Doe - Java Developer");
    }

    @Test
    void rejectsUnsupportedFileType() {
        var file = new MockMultipartFile("file", "resume.docx", "application/octet-stream", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> extractor.extract(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(".pdf, .txt or .md");
    }

    @Test
    void rejectsEmptyFile() {
        var file = new MockMultipartFile("file", "resume.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> extractor.extract(file)).hasMessageContaining("choose a resume");
    }

    @Test
    void truncatesVeryLongResume() {
        var file = new MockMultipartFile("file", "resume.txt", "text/plain",
                "a".repeat(ResumeTextExtractor.MAX_CHARS + 500).getBytes(StandardCharsets.UTF_8));

        assertThat(extractor.extract(file)).hasSize(ResumeTextExtractor.MAX_CHARS);
    }

    private static byte[] pdfWith(String text) throws Exception {
        try (PDDocument doc = new PDDocument(); var out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (var content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 700);
                content.showText(text);
                content.endText();
            }
            doc.save(out);
            return out.toByteArray();
        }
    }
}
