package org.sistemagestion.backend.config;

import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

// Identifica al dueno de los datos con el claim "sub" del token de Keycloak
// Hibernate lo usa para filtrar toda lectura (@TenantId) y para completar owner_id al guardar
@Component
public class CurrentOwnerResolver implements CurrentTenantIdentifierResolver<UUID>, HibernatePropertiesCustomizer {

    // Sin usuario autenticado, se devuelve un dueno que no existe: ninguna consulta ve datos
    private static final UUID NO_OWNER = new UUID(0L, 0L);

    @Override
    public UUID resolveCurrentTenantIdentifier() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return UUID.fromString(jwtAuthentication.getToken().getSubject());
        }

        return NO_OWNER;
    }

    // Si la sesion de Hibernate ya estaba abierta para otro dueño, falla en vez de mezclar datos
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
