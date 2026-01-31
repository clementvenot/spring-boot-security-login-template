package com.template.controller;

import com.template.mapper.UserMapper;
import com.template.service.AuthService;
import com.template.dto.ForgotPasswordRequestDTO;
import com.template.service.ForgotPasswordService;
import com.template.dto.ResetPasswordRequestDTO;
import com.template.dto.LoginRequestDTO;
import com.template.dto.RegisterRequestDTO;
import com.template.dto.UserResponseDTO;
import com.template.security.jwt.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for login, logout, registration, and password reset")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService service;
    private final JwtService jwt;
    private final ForgotPasswordService forgotPasswordService;

    @Value("${app.front.reset-url}")
    private String frontResetUrlBase;

    public AuthController(AuthService service, JwtService jwt, ForgotPasswordService forgotPasswordService) {
        this.service = service;
        this.jwt = jwt;
        this.forgotPasswordService = forgotPasswordService;
    }

    @Operation(
        summary = "Log in and set the access token cookie",
        description = """
            Authenticates a user and sets a short-lived JWT in an HttpOnly cookie named `access_token`.
            The cookie uses `SameSite=None` (cross-site) and should be `secure=true` in production (HTTPS).
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = LoginRequestDTO.class),
                examples = @ExampleObject(
                    name = "Default",
                    value = """
                    { "email": "john.doe@example.com", "password": "Password123!" }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully authenticated. The `access_token` cookie is set.",
                headers = {
                    @Header(
                        name = "Set-Cookie",
                        description = "HttpOnly cookie containing a short-lived JWT (`access_token`).",
                        schema = @Schema(type = "string")
                    )
                },
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class),
                    examples = @ExampleObject(
                        name = "Default",
                        value = """
                        { "id": 42, "email": "john.doe@example.com", "firstName": "John", "lastName": "Doe" }
                        """
                    )
                )
            ),
            @ApiResponse(responseCode = "400", description = "Validation error."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        var user = service.login(dto);

        String accessToken = jwt.generateToken(
            user.getEmail(),
            15 * 60,
            Map.of("roles", user.getRoles())
        );

        ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false) // false for HTTP (dev), true for HTTPS (prod)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(UserMapper.toDTO(user));
    }

    @Operation(
        summary = "Log out and clear the access token cookie",
        description = "Logs out the current user by clearing the `access_token` cookie.",
        security = { @SecurityRequirement(name = "cookieAuth") },
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Successfully logged out. The `access_token` cookie is cleared.",
                headers = {
                    @Header(
                        name = "Set-Cookie",
                        description = "Clearing cookie (`access_token`) with Max-Age=0.",
                        schema = @Schema(type = "string")
                    )
                }
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false) // set to true in production
                .path("/")
                .maxAge(Duration.ZERO)  // delete cookie
                .sameSite("None")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = RegisterRequestDTO.class),
                examples = @ExampleObject(
                    name = "Default",
                    value = """
                    {
                      "email": "john.doe@example.com",
                      "password": "Password123!",
                      "firstName": "John",
                      "lastName": "Doe"
                    }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "User successfully registered",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already used")
        }
    )
    @PostMapping("/register")
    public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO dto) {
        var user = service.register(dto);
        return UserMapper.toDTO(user);
    }

    @Operation(
        summary = "Request password reset (anti-enumeration)",
        description = """
            Triggers a password reset flow by sending a reset link if the email exists.
            For anti-enumeration, the response is always 200 with a generic message.
            Rate-limited by IP and email (see server configuration).
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = ForgotPasswordRequestDTO.class),
                examples = @ExampleObject(
                    name = "Default",
                    value = """
                    { "email": "john.doe@example.com" }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Always returns a generic message to avoid email enumeration.",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(example = "If the email exists, a reset link has been sent.")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "429", description = "Too many requests (rate limited)"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
        }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request,
                                            HttpServletRequest http) {
        String ip = extractClientIp(http);
        forgotPasswordService.requestReset(request.email(), ip, frontResetUrlBase);
        // Anti-disclosure: always return OK to avoid revealing whether the email exists
        return ResponseEntity.ok("If the email exists, a reset link has been sent.");
    }

    @Operation(
        summary = "Reset password using a token",
        description = """
            Resets the password if the provided token is valid and not expired.
            Returns 400 for invalid or expired tokens.
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = ResetPasswordRequestDTO.class),
                examples = @ExampleObject(
                    name = "Default",
                    value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "newPassword": "NewStrongPassword123!"
                    }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Password has been reset.",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(example = "Password has been reset.")
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid or expired token.",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(example = "Invalid or expired token.")
                )
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
        }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        // Security tip: avoid logging full tokens in production. Prefer a short prefix/suffix if needed.
        log.info("Reset request received"); // previously: token='{}'
        boolean ok = forgotPasswordService.resetPassword(request.token(), request.newPassword());
        if (!ok) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
        return ResponseEntity.ok("Password has been reset.");
    }

    /**
     * Extracts the client IP from reverse proxy headers when present.
     * Checks `X-Forwarded-For` (first entry) and `X-Real-IP` before falling back to the remote address.
     */
    private String extractClientIp(HttpServletRequest request) {
        // Proxy headers handling
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        String xr = request.getHeader("X-Real-IP");
        if (xr != null && !xr.isBlank()) {
            return xr.trim();
        }
        return request.getRemoteAddr();
    }
}