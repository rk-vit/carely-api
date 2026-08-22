package com.carely.users.dto;

import java.util.UUID;

public record CreateUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String role,
        String status
) {
}