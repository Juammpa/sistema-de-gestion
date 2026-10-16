package org.sistemagestion.backend.integration.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.IdentityProviderException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.model.enums.UserRole;
import org.sistemagestion.backend.service.user.IdentityProviderClient;
import org.sistemagestion.backend.service.user.IdentityUser;
import org.sistemagestion.backend.service.user.NewIdentityUser;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;


// Adaptador de Keycloak: traduce su Admin REST API (JSON de Keycloak) al modelo del sistema.
// Es la unica clase que conoce rutas y detalles de Keycloak
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClient implements IdentityProviderClient {

    // Tipo con genericos para que Jackson deserialice una lista de KeycloakUser
    private static final ParameterizedTypeReference<List<KeycloakUser>> USER_LIST = new ParameterizedTypeReference<List<KeycloakUser>>() {};

    // Para leer y reenviar el usuario completo sin depender de todos sus campos
    private static final ParameterizedTypeReference<Map<String, Object>> USER_MAP = new ParameterizedTypeReference<Map<String, Object>>() {};

    // Tope de miembros que se leen por rol
    private static final int ROLE_MEMBERS_LIMIT = 1000;

    private final RestClient keycloakAdminRestClient;

    @Override
    public List<IdentityUser> findUsers(String search, int first, int max) {

        List<KeycloakUser> users = call(() ->
                keycloakAdminRestClient.get()
                        .uri(uri -> {
                            UriBuilder builder = uri.path("/users").queryParam("first", first).queryParam("max", max);

                            if (StringUtils.hasText(search)) {
                                // Como variable de plantilla para que Spring codifique caracteres como + o &
                                return builder.queryParam("search", "{search}").build(search);
                            }

                            return builder.build();
                        }). retrieve().body(USER_LIST));

        return orEmpty(users).stream().map(KeycloakClient::toIdentityUser).toList();
    }

    @Override
    public List<IdentityUser> findUsersByRole(UserRole role) {

        List<KeycloakUser> members = orEmpty(call(() ->
                keycloakAdminRestClient.get()
                        .uri("/roles/{role}/users?first=0&max={max}", role.name(), ROLE_MEMBERS_LIMIT)
                        .retrieve().body(USER_LIST)));

        if (members.size() >= ROLE_MEMBERS_LIMIT) {
            log.warn("El rol {} alcanzó el tope de {} miembros leídos: algunos usuarios podrían mostrarse sin rol", role, ROLE_MEMBERS_LIMIT);
        }

        return members.stream().map(KeycloakClient::toIdentityUser).toList();
    }

    @Override
    public IdentityUser findUserById(String userId) {

        KeycloakUser user = call(() -> keycloakAdminRestClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                    throw new ResourceNotFoundException("User","id",userId);
                }).body(KeycloakUser.class));

        return toIdentityUser(user);
    }

    @Override
    public String createUser(NewIdentityUser user, String temporaryPassword) {

        // enabled y emailVerified en true: no hay SMTP para verificar el correo.
        // temporary=true obliga a cambiar la contraseña en el primer ingreso
        KeycloakNewUser body = new KeycloakNewUser(
                user.username(), user.email(), user.firstName(), user.lastName(), true, true,
                List.of(new KeycloakCredential("password", temporaryPassword, true)));

        URI location = call(() -> keycloakAdminRestClient.post()
                .uri("/users")
                .body(body)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (request, response) -> {
                    throw new BusinessRuleException("Ya existe un usuario con ese nombre de usuario o correo.");
                })
                .onStatus(status -> status.value() == HttpStatus.BAD_REQUEST.value(), (request, response) -> {
                    throw new BusinessRuleException(
                            "No se pudo crear el usuario: la contraseña no cumple con la política de seguridad o algún dato no es válido.");
                })
                .toBodilessEntity()
                .getHeaders()
                .getLocation());

        if (location == null) {
            throw new IdentityProviderException("Keycloak no devolvío la ubicación del usuario creado",null);
        }

        // El id es el ultimo tramo de la URL: .../users/{id}
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);

    }

    @Override
    public void assignRole(String userId, UserRole role) {

        execute(() -> addRealmRole(userId, role));
    }

    @Override
    public void changeRole(String userId, UserRole newRole) {

        // Primero agrega el rol nuevo y despues quita los demas.
        // Si algo falla a mitad de camino, el usuario nunca queda sin rol
        execute(() -> {
            addRealmRole(userId, newRole);

            for (UserRole other: UserRole.values()) {
                if (other != newRole) {
                    removeRealmRole(userId, other);
                }
            }
        });

    }

    @Override
    public void deleteUser(String userId) {

        execute(() -> keycloakAdminRestClient.delete()
                .uri("/users/{id}", userId)
                .retrieve()
                .toBodilessEntity());

    }

    @Override
    public void setEnabled(String userId, boolean enabled) {

        execute(() -> {
            Map<String, Object> user = keycloakAdminRestClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                            (request, response) -> {
                                throw new ResourceNotFoundException("User", "id", userId);
                            }).body(USER_MAP);

            user.put("enabled", enabled);

            keycloakAdminRestClient.put()
                    .uri("/users/{id}", userId)
                    .body(user)
                    .retrieve()
                    .toBodilessEntity();
        });
    }

    @Override
    public void closeSessions(String userId) {

        execute(() -> keycloakAdminRestClient.post()
                .uri("/users/{id}/logout", userId)
                .retrieve()
                .toBodilessEntity());
    }

    // Cualquier falla al hablar con Keycloak (red, timeout, token, 4xx/5xx) se traduce a una excepcion propia
    private <T> T call(Supplier<T> request) {

        try {
            return request.get();
        } catch (RestClientException | OAuth2AuthorizationException ex) {

            log.error("Falló la llamada a Keycloak: {}", ex.getMessage());
            throw new IdentityProviderException("Keycloak no respondió correctamente", ex);
        }
    }

    // Igual que call, para operaciones que no devuelven resultado
    private void execute(Runnable request) {
        call(() -> {
            request.run();
            return null;
        });
    }

    // Si Keycloak responde sin cuerpo, se trata como lista vacia
    private static <T> List<T> orEmpty(List<T> list) {
        return Objects.requireNonNullElse(list, List.of());
    }

    // Keycloak pide el rol completo (id y nombre) para asignarlo o quitarlo
    private KeycloakRole findRealmRole(UserRole role) {

        return keycloakAdminRestClient.get()
                .uri("/roles/{role}", role.name())
                .retrieve()
                .body(KeycloakRole.class);
    }

    private void addRealmRole(String userId, UserRole role) {

        keycloakAdminRestClient.post()
                .uri("/users/{id}/role-mappings/realm", userId)
                .body(List.of(findRealmRole(role)))
                .retrieve()
                .toBodilessEntity();
    }

    // En Keycloak el DELETE de roles lleva el rol en el cuerpo del pedido
    private void removeRealmRole(String userId, UserRole role) {

        keycloakAdminRestClient.method(HttpMethod.DELETE)
                .uri("/users/{id}/role-mappings/realm", userId)
                .body(List.of(findRealmRole(role)))
                .retrieve()
                .toBodilessEntity();
    }

    private static IdentityUser toIdentityUser(KeycloakUser user) {

        return new IdentityUser(
                user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
                Boolean.TRUE.equals(user.enabled()),
                user.createdTimestamp() == null ? null : Instant.ofEpochMilli(user.createdTimestamp()));
    }
}
