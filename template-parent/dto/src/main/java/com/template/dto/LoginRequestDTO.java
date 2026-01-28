package com.template.dto;

import com.template.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank
        @Email
        @Size(max = 254) 
        String email,

        @NotBlank
        @StrongPassword(message = "Password must be 12–64 chars and include upper, lower, digit and symbol.")        
        String password
) {}
