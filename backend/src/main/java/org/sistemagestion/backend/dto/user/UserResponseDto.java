package org.sistemagestion.backend.dto.user;

import org.sistemagestion.backend.model.enums.UserRole;

import java.time.Instant;

public record UserResponseDto(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        UserRole role,
        Instant createdAt
) { }
