package com.template.front.client;

import com.template.dto.LoginRequestDTO;
import com.template.dto.RegisterRequestDTO;
import com.template.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;

@Component
public class BackendAuthClient {

    private final RestTemplate rest;
    private final String baseUrl;
    private final String authCookieName;

    public BackendAuthClient(RestTemplate rest,
    						@Value("${backend.api.url}") String baseUrl,
                            @Value("${app.auth-cookie-name}") String authCookieName) {
        this.rest = rest;
        this.baseUrl = baseUrl;
        this.authCookieName = authCookieName;
    }

    public static class LoginResult {
        public final UserResponseDTO user;
        public final String accessTokenCookieValue; // cookie value
        public final String tokenOnly; // JWT value

        public LoginResult(UserResponseDTO user, String accessTokenCookieValue, String tokenOnly) {
            this.user = user;
            this.accessTokenCookieValue = accessTokenCookieValue;
            this.tokenOnly = tokenOnly;
        }
    }

    public LoginResult login(LoginRequestDTO dto) {
        String url = baseUrl + "/auth/login";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequestDTO> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<UserResponseDTO> response =
                rest.exchange(url, HttpMethod.POST, entity, UserResponseDTO.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Login failed with status " + response.getStatusCode());
        }

        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookies == null || setCookies.isEmpty()) {
            throw new IllegalStateException("No Set-Cookie returned by backend during login");
        }

        Optional<String> tokenCookie = setCookies.stream()
                .filter(c -> c.toLowerCase().startsWith(authCookieName.toLowerCase() + "="))
                .findFirst();

        if (tokenCookie.isEmpty()) {
            // if setCookie is not first
            String tokenOnly = extractTokenFromSetCookieList(setCookies, authCookieName)
                    .orElseThrow(() -> new IllegalStateException("No access_token cookie found"));
            // rebuild Set-Cookie 
            String rebuilt = authCookieName + "=" + tokenOnly + "; Path=/; HttpOnly";
            return new LoginResult(response.getBody(), rebuilt, tokenOnly);
        } else {
            String fullCookie = tokenCookie.get();
            String tokenOnly = extractTokenValue(fullCookie, authCookieName)
                    .orElseThrow(() -> new IllegalStateException("Token parse error"));
            return new LoginResult(response.getBody(), fullCookie, tokenOnly);
        }
    }

    public UserResponseDTO register(RegisterRequestDTO dto) {
        String url = baseUrl + "/auth/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegisterRequestDTO> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<UserResponseDTO> response =
                rest.exchange(url, HttpMethod.POST, entity, UserResponseDTO.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Register failed with status " + response.getStatusCode());
        }
        return response.getBody();
    }

    public void logout(String tokenFromSession) {
        String url = baseUrl + "/auth/logout";
        HttpHeaders headers = new HttpHeaders();
        // backend waiting cookie access_token
        headers.add(HttpHeaders.COOKIE, authCookieName + "=" + tokenFromSession);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        rest.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    private static Optional<String> extractTokenValue(String setCookie, String name) {
        String prefix = name + "=";
        int idx = setCookie.indexOf(prefix);
        if (idx >= 0) {
            int start = idx + prefix.length();
            int end = setCookie.indexOf(';', start);
            if (end < 0) end = setCookie.length();
            return Optional.of(setCookie.substring(start, end));
        }
        return Optional.empty();
    }

    private static Optional<String> extractTokenFromSetCookieList(List<String> cookies, String name) {
        for (String c : cookies) {
            var maybe = extractTokenValue(c, name);
            if (maybe.isPresent()) return maybe;
        }
        return Optional.empty();
    }
}
