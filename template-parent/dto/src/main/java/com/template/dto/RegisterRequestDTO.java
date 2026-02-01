package com.template.dto;

import com.template.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload used to create a new user account.")
public record RegisterRequestDTO(

        @Schema(
                description = "User email address used for account registration. Must be unique and valid.",
                example = "john.doe@example.com"
        )
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        String email,

        @Schema(
                description = "User password. Must meet strong password complexity requirements.",
                example = "MyStr0ng!Password2024"
        )
        @NotBlank(message = "Password is required.")
        @StrongPassword(message = "Password must be 12–64 chars and include upper, lower, digit and symbol.")
        String password,

        @Schema(
                description = "User's first name.",
                example = "John",
                minLength = 1,
                maxLength = 50
        )
        @NotBlank(message = "First name is required.")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters.")
        String firstName,

        @Schema(
                description = "User's last name.",
                example = "Doe",
                minLength = 1,
                maxLength = 50
        )
        @NotBlank(message = "Last name is required.")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters.")
        String lastName
) {}