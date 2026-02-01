package com.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.template.validation.StrongPassword;

@Schema(description = "Request payload used to reset a user's password using a valid reset token.")
public record ResetPasswordRequestDTO(

        @Schema(
                description = "Reset token sent to the user's email. Must be valid and not expired.",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @NotBlank
        String token,

        @Schema(
                description = "New password chosen by the user. Must comply with strong password requirements.",
                example = "NewStr0ng!Password2024",
                minLength = 8,
                maxLength = 64
        )
        @NotBlank
        @StrongPassword(min = 8, max = 64)
        @Size(min = 8, max = 64)
        String newPassword
) {}