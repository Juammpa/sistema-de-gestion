package org.sistemagestion.backend.service.user;

import java.time.Instant;

public record IdentityUser(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Instant createdAt
) {}
