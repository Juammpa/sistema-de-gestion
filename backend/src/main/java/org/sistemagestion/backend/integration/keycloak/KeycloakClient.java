package org.sistemagestion.backend.integration.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.exception.IdentityProviderException;
import org.sistemagestion.backend.model.enums.UserRole;
import org.sistemagestion.backend.service.user.IdentityProviderClient;
import org.sistemagestion.backend.service.user.IdentityUser;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClient implements IdentityProviderClient {

    private static final ParameterizedTypeReference<List<KeycloakUser>> USER_LIST = new ParameterizedTypeReference<List<KeycloakUser>>() {};

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
    public Set<String> findUserIdsByRole(UserRole role) {

        List<KeycloakUser> members = call(() ->
                keycloakAdminRestClient.get()
                        .uri("/roles/{role}/users?first=0&max={max}", role.name(), ROLE_MEMBERS_LIMIT)
                        .retrieve().body(USER_LIST));

        return orEmpty(members).stream().map(KeycloakUser::id).collect(Collectors.toSet());
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

    private static <T> List<T> orEmpty(List<T> list) {
        return Objects.requireNonNullElse(list, List.of());
    }

    private static IdentityUser toIdentityUser(KeycloakUser user) {

        return new IdentityUser(
                user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
                Boolean.TRUE.equals(user.enabled()),
                user.createdTimestamp() == null ? null : Instant.ofEpochMilli(user.createdTimestamp()));
    }
}
