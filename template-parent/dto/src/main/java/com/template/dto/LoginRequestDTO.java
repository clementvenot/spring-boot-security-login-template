package com.template.dto;

import com.template.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload used to authenticate a user.")
public record LoginRequestDTO(

        @Schema(
                description = "User email address used to log in. Must be a valid email.",
                example = "john.doe@example.com",
                maxLength = 254
        )
        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @Schema(
                description = "User password. Must meet strong password requirements.",
                example = "Str0ng!Password2024"
        )
        @NotBlank
        @StrongPassword(
                message = "Password must be 12–64 chars and include upper, lower, digit and symbol."
        )
        String password
) {}