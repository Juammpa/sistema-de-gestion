package org.sistemagestion.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void mapsRealmRolesToRoleAuthorities() {
        Jwt jwt = jwtWith("realm_access", Map.of("roles", List.of("ADMIN", "USER")));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void returnsNoAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = jwtWith("scope", "profile");

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void returnsNoAuthoritiesWhenRolesIsNotACollection() {
        Jwt jwt = jwtWith("realm_access", Map.of("roles", "ADMIN"));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    private Jwt jwtWith(String claimName, Object claimValue) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-1")
                .claim(claimName, claimValue)
                .build();
    }
}