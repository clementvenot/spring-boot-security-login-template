
package com.template.controller;

import com.template.mapper.UserMapper;
import com.template.service.AuthService;
import com.template.dto.RegisterRequestDTO;
import com.template.dto.LoginRequestDTO;
import com.template.dto.UserResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with basic personal information.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User successfully registered",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request payload",
                content = @Content
            )
        }
    )
    public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO dto) {
        return UserMapper.toDTO(service.register(dto));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate a user",
        description = "Logs in a user using email and password.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User successfully authenticated",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid email or password",
                content = @Content
            )
        }
    )
    public UserResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return UserMapper.toDTO(service.login(dto));
    }
}
