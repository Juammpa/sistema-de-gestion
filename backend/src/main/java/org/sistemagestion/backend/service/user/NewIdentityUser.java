package org.sistemagestion.backend.service.user;

public record NewIdentityUser(
        String username,
        String email,
        String firstName,
        String lastName
) {}
