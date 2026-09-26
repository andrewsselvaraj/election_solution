package com.election.solution.controller;

import com.election.solution.ai.ResumeAssistant;
import com.election.solution.resume.ResumeTextExtractor;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Upload a resume, then ask questions about it. The resume text lives only in the
 * HTTP session (memory) - it is never written to disk or committed.
 */
@Controller
@RequestMapping("/resume")
public class ResumeController {

    static final String RESUME_TEXT = "resumeText";
    static final String RESUME_NAME = "resumeName";
    static final String CONVERSATION_ID = "resumeConversationId";

    private final ObjectProvider<ResumeAssistant> assistant;
    private final ResumeTextExtractor extractor;

    public ResumeController(ObjectProvider<ResumeAssistant> assistant, ResumeTextExtractor extractor) {
        this.assistant = assistant;
        this.extractor = extractor;
    }

    @GetMapping
    public String page(HttpSession session, Model model) {
        return render(session, model);
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, HttpSession session, Model model) {
        try {
            session.setAttribute(RESUME_TEXT, extractor.extract(file));
            session.setAttribute(RESUME_NAME, file.getOriginalFilename());
            // new resume = new conversation, so old answers are not remembered
            session.setAttribute(CONVERSATION_ID, UUID.randomUUID().toString());
            model.addAttribute("message", "Resume loaded. Ask your question below.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return render(session, model);
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question, HttpSession session, Model model) {
        ResumeAssistant ai = assistant.getIfAvailable();
        String resume = (String) session.getAttribute(RESUME_TEXT);
        model.addAttribute("question", question);
        if (resume == null) {
            model.addAttribute("error", "Upload your resume first.");
        } else if (ai != null && !question.isBlank()) {
            String conversationId = (String) session.getAttribute(CONVERSATION_ID);
            model.addAttribute("answer", ai.ask(conversationId, resume, question));
        }
        return render(session, model);
    }

    private String render(HttpSession session, Model model) {
        model.addAttribute("title", "Resume Assistant");
        model.addAttribute("enabled", assistant.getIfAvailable() != null);
        model.addAttribute("resumeName", session.getAttribute(RESUME_NAME));
        return "resume";
    }
}
