package com.template.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordPageController {

    @GetMapping("/forgot-password")
    public String resetPasswordPage(
            @RequestParam(value = "token", required = false) String token,
            Model model) {

        model.addAttribute("token", token);

        if (token == null || token.isBlank()) {
            model.addAttribute("noToken", true); // to display the ‘no‑token’ mode
        }

        return "forgot-password"; // Thymeleaf template: forgot-password.html
    }
}
