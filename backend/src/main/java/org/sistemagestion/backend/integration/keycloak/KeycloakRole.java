package org.sistemagestion.backend.integration.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakRole(String id, String name) {}
