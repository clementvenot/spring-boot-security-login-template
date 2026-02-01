package com.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload used to initiate a password reset process.")
public record ForgotPasswordRequestDTO(

        @Schema(
                description = "User email address used to request a password reset. " +
                              "Must be a valid and existing email.",
                example = "john.doe@example.com",
                maxLength = 254
        )
        @NotBlank
        @Email
        @Size(max = 254)
        String email
) {}