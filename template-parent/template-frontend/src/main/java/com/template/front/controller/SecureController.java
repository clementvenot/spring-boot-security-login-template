package com.template.front.controller;

import com.template.dto.UserResponseDTO;
import com.template.front.config.WebConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class SecureController {

    @GetMapping("/")
    public String root() {
        return "redirect:/secure";
    }

    @GetMapping("/secure")
    public String securePage(HttpSession session, Model model) {
        var user = (UserResponseDTO) session.getAttribute(WebConfig.SESSION_USER);
        model.addAttribute("user", user);
        return "secure";
    }
}
