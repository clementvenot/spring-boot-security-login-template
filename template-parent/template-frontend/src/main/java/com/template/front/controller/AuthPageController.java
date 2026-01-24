package com.template.front.controller;

import com.template.dto.LoginRequestDTO;
import com.template.dto.RegisterRequestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("form")) {
        	model.addAttribute("form", new LoginRequestDTO("", ""));
        	}
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
        	model.addAttribute("form", new RegisterRequestDTO("", "", "", ""));
        	}
        return "register";
    }
}
