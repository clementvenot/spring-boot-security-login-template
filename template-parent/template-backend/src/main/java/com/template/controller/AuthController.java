package com.template.controller;

import com.template.mapper.UserMapper;
import com.template.service.AuthService;
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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login and logout endpoints")
public class AuthController {

    private final AuthService service;
    private final JwtService jwt;

    public AuthController(AuthService service, JwtService jwt) {
        this.service = service;
        this.jwt = jwt;
    }

    @Operation(
        summary = "Log in and set the access token cookie",
        description = "Authenticates a user and sets a short-lived JWT in an HttpOnly cookie named `access_token`.",
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
                .secure(false) //false for http, true for https
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
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
    
	@PostMapping("/register")
	@Operation(
	    summary = "Register a new user",
	    description = "Creates a new user account.",
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "User successfully registered"
	        ),
	        @ApiResponse(responseCode = "400", description = "Validation error"),
	        @ApiResponse(responseCode = "409", description = "Email already used")
	    }
	)
	public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO dto) {
	    var user = service.register(dto);
	    return UserMapper.toDTO(user);
	}

}
