package com.election.solution.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Election Solution");
        model.addAttribute("message", "Welcome to the Election Solution application.");
        return "index";
    }
}
