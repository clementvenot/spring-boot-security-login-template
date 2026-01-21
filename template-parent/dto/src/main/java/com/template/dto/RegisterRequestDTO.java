package com.template.dto;

public record RegisterRequestDTO(
        String email,
        String password,
        String firstName,
        String lastName
) {}
