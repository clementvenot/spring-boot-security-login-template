package com.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload representing a user returned by the system.")
public record UserResponseDTO(

        @Schema(
                description = "Unique identifier of the user.",
                example = "42"
        )
        Long id,

        @Schema(
                description = "User email address.",
                example = "john.doe@example.com"
        )
        String email,

        @Schema(
                description = "User's first name.",
                example = "John"
        )
        String firstName,

        @Schema(
                description = "User's last name.",
                example = "Doe"
        )
        String lastName
) {}