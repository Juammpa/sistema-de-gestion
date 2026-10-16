package org.sistemagestion.backend.integration.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Forma del usuario en el JSON de Keycloak.
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakUser(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Long createdTimestamp
) {}
