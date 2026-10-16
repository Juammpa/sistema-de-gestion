package org.sistemagestion.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

// Acceso del backend a la Admin API de Keycloak con la service account "backend-admin"
@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakAdminConfig {

    private static final String REGISTRATION_ID = "keycloak-admin";

    // Pide y renueva el token de la service account automaticamente, sin codigo propio de cache.
    @Bean
    OAuth2AuthorizedClientManager keycloakAdminClientManager(ClientRegistrationRepository registrations, OAuth2AuthorizedClientService authorizedClients) {

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, authorizedClients);

        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());

        return manager;
    }

    // Cliente apuntado a /admin/realms/{realm}: cada request lleva el token y respeta timeouts
    @Bean
    RestClient keycloakAdminRestClient(KeycloakProperties properties, OAuth2AuthorizedClientManager clientManager) {

        // Un unico "principal" fijo: el token de la service account se pide una vez y se reutiliza
        // para todos los administradores, en vez de uno por cada usuario que llama la API
        Authentication servicePrincipal = new AnonymousAuthenticationToken(
                REGISTRATION_ID, REGISTRATION_ID, AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        OAuth2ClientHttpRequestInterceptor tokenInterceptor = new OAuth2ClientHttpRequestInterceptor(clientManager);
        tokenInterceptor.setClientRegistrationIdResolver(request -> REGISTRATION_ID);
        tokenInterceptor.setPrincipalResolver(request -> servicePrincipal);

        // Con timeouts: si Keycloak se cuelga, no deben quedarse colgados los hilos
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(properties.baseUrl() + "/admin/realms/" + properties.realm())
                .requestFactory(requestFactory)
                .requestInterceptor(tokenInterceptor)
                .build();
    }
}
