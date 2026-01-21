package com.template.dto;

public record UserResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName
) {}
