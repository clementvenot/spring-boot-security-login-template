package com.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.template.validation.StrongPassword;

public record ResetPasswordRequestDTO(
        @NotBlank
        String token,
        @NotBlank
        @StrongPassword(min = 8, max = 64)
        @Size(min = 8, max = 64)
        String newPassword
) {}
