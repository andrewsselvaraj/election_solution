package com.election.solution.controller;

import com.election.solution.service.GeminiChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/gemini")
public class GeminiController {

    private final GeminiChatService geminiChatService;

    public GeminiController(GeminiChatService geminiChatService) {
        this.geminiChatService = geminiChatService;
    }

    @GetMapping
    public String page(Model model) {
        return render(model);
    }

    @PostMapping
    public String ask(@RequestParam String question, HttpSession session, Model model) {
        model.addAttribute("question", question);
        try {
            model.addAttribute("answer", geminiChatService.ask(session.getId(), question));
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }
        return render(model);
    }

    private String render(Model model) {
        model.addAttribute("title", "Gemini Chat");
        model.addAttribute("enabled", geminiChatService.isEnabled());
        return "gemini";
    }
}
