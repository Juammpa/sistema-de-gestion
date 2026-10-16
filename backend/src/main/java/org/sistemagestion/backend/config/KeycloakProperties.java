package org.sistemagestion.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Datos de conexion a Keycloak
@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties (String baseUrl, String realm){}
