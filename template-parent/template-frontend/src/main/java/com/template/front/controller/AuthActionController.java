package com.template.front.controller;

import com.template.dto.LoginRequestDTO;
import com.template.dto.RegisterRequestDTO;
import com.template.dto.UserResponseDTO;
import com.template.front.client.BackendAuthClient;
import com.template.front.config.WebConfig;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthActionController {

    private final BackendAuthClient backend;
    private final String authCookieName;

    public AuthActionController(BackendAuthClient backend,
                                @Value("${app.auth-cookie-name}") String authCookieName) {
        this.backend = backend;
        this.authCookieName = authCookieName;
    }

    @PostMapping("/login")
    public String doLogin(@Validated @ModelAttribute("form") LoginRequestDTO form,
                          BindingResult binding,
                          HttpServletResponse response,
                          HttpSession session,
                          Model model) {
        if (binding.hasErrors()) {
            return "login";
        }

        try {
            var res = backend.login(form);

            // stock token user
            session.setAttribute(WebConfig.SESSION_JWT, res.tokenOnly);
            session.setAttribute(WebConfig.SESSION_USER, res.user);

            ResponseCookie cookie = ResponseCookie.from(authCookieName, res.tokenOnly)
                    .httpOnly(true)
                    .secure(false)  // false for http, true for http
                    .path("/")
                    .maxAge(15 * 60) // 15 min 
                    .sameSite("None")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return "redirect:/secure";
        } catch (Exception e) {
            model.addAttribute("loginError", "Email ou mot de passe invalide.");
            return "login";
        }
    }

    @PostMapping("/register")
    public String doRegister(@Validated @ModelAttribute("form") RegisterRequestDTO form,
                             BindingResult binding,
                             Model model) {
        if (binding.hasErrors()) {
            return "register";
        }

        try {
            UserResponseDTO user = backend.register(form);
            model.addAttribute("registeredEmail", user.email());
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("registerError", "Inscription impossible (email déjà utilisé ?).");
            return "register";
        }
    }

    @PostMapping("/logout")
    public String doLogout(HttpServletResponse response, HttpSession session) {
        var token = (String) session.getAttribute(WebConfig.SESSION_JWT);
        if (token != null) {
            try {
                backend.logout(token);
            } catch (Exception ignored) {
            }
        }
        // clean session
        session.invalidate();

        // clean cookie 
        ResponseCookie cookie = ResponseCookie.from(authCookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return "redirect:/login?logout";
    }
}
