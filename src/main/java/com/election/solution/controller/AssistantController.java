package com.election.solution.controller;

import com.election.solution.ai.ElectionAssistant;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AssistantController {

    private final ObjectProvider<ElectionAssistant> assistant;

    public AssistantController(ObjectProvider<ElectionAssistant> assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/assistant")
    public String form(Model model) {
        model.addAttribute("title", "Election Assistant");
        model.addAttribute("enabled", assistant.getIfAvailable() != null);
        return "assistant";
    }

    @PostMapping("/assistant")
    public String ask(@RequestParam String question, Model model) {
        ElectionAssistant ai = assistant.getIfAvailable();
        model.addAttribute("title", "Election Assistant");
        model.addAttribute("enabled", ai != null);
        model.addAttribute("question", question);
        if (ai != null && !question.isBlank()) {
            model.addAttribute("answer", ai.chat(question));
        }
        return "assistant";
    }
}
