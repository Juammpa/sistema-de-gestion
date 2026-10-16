package org.sistemagestion.backend.integration.keycloak;

import java.util.List;

public record KeycloakNewUser(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean emailVerified,
        List<KeycloakCredential> credentials
) {}
