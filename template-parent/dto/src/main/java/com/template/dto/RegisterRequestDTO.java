package com.template.dto;

import com.template.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "Password is required.")        
        @StrongPassword(message = "Password must be 12–64 chars and include upper, lower, digit and symbol.")        
        String password,
        
        @NotBlank(message = "First name is required.")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters.")
        String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters.")
        String lastName
) {}