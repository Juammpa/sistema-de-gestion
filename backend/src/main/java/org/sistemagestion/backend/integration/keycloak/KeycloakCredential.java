package org.sistemagestion.backend.integration.keycloak;

public record KeycloakCredential(
        String type, String value, boolean temporary) {

    // Nunca debe aparecer la contraseña en un log

    @Override
    public String toString() {
        return "KeycloakCredential[type=" + type + ", value=***, temporary=" + temporary + "]";
    }
}
