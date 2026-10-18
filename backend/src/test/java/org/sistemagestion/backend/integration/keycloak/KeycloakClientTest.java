package org.sistemagestion.backend.integration.keycloak;

import org.junit.jupiter.api.*;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.IdentityProviderException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.model.enums.UserRole;
import org.sistemagestion.backend.service.user.IdentityUser;
import org.sistemagestion.backend.service.user.NewIdentityUser;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class KeycloakClientTest {

    private static final String BASE_URL = "http://keycloak.test/admin/realms/sistema-gestion";

    private MockRestServiceServer server;
    private KeycloakClient client;

    @BeforeEach
    void setUp() {
        // Se arma el mismo tipo de RestClient que usa la app, pero con respuestas simuladas.
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KeycloakClient(builder.build());
    }

    @AfterEach
    void tearDown() {
        // Falla si el codigo no hizo alguna de las llamadas que el test esperaba
        server.verify();
    }

    @Nested
    @DisplayName("findUsers()")
    class FindUsersTests {

        @Test
        @DisplayName("Debe enviar solo la paginación cuando no hay filtros")
        void findUsersSendsOnlyThePaginationWhenThereAreNoFilters() {

            // Arrange
            // La URL exacta comprueba que no se envian "search" ni "enabled"
            server.expect(requestTo(BASE_URL + "/users?first=20&max=21"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsers(null, null, 20, 21);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe codificar el texto de búsqueda para que los caracteres especiales no alteren la consulta")
        void findUsersEncodesTheSearchTextSoSpecialCharactersCannotAlterTheQuery() {

            // Arrange
            // "+" y "&" son caracteres con significado propio en una URL
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21&search=a%2Bb%26c"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsers("a+b&c", null, 0, 21);

            // Assert
            assertThat(result).isEmpty();

        }

        @Test
        @DisplayName("Debe enviar el filtro de estado junto con el texto de búsqueda")
        void findUsersSendsTheStatusFilterTogetherWithTheSearchText() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21&enabled=true&search=juan"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsers("juan", true, 0, 21);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe usar búsqueda con comodín cuando solo se filtra por estado, para ocultar las cuentas de servicio")
        void findUsersUsesAWildcardSearchWhenOnlyTheStatusIsFiltered() {

            // Arrange
            // Sin "search", Keycloak incluiria las cuentas de servicio.
            // Con "search", las excluye, y "*" coincide con todos. Viaja como %2A
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21&enabled=false&search=%2A"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsers(null, false, 0, 21);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe tratar un texto en blanco como sin búsqueda cuando se filtra por estado")
        void findUsersTreatsABlankSearchAsNoSearchWhenFilteringByStatus() {

            // Arrange
            // Un texto con solo espacios no es una busqueda
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21&enabled=true&search=%2A"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsers("  ", true, 0, 21);

            // Assert
            assertThat(result).isEmpty();

        }

        @Test
        @DisplayName("Debe convertir los campos de Keycloak al usuario del sistema e ignorar los que no usa")
        void findUsersMapsTheKeycloakFieldsToTheDomainUser() {

            // Arrange
            // La respuesta trae campos que el sistema no usa (emailVerified, access) y deben ignorarse.
            String json = """
                    [{
                      "id": "u-1",
                      "username": "juan",
                      "email": "juan@mail.com",
                      "firstName": "Juan",
                      "lastName": "Perez",
                      "enabled": true,
                      "createdTimestamp": 1767225600000,
                      "emailVerified": true,
                      "access": { "manage": true }
                    }]
                    """;

            server.expect(requestTo(BASE_URL + "/users?first=0&max=21"))
                    .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsers(null, null, 0, 21);

            // Assert
            assertThat(result).containsExactly(new IdentityUser(
                    "u-1","juan","juan@mail.com", "Juan", "Perez",
                    true, Instant.parse("2026-01-01T00:00:00Z")));
        }

        @Test
        @DisplayName("Debe considerar deshabilitado y sin fecha de alta a un usuario con campos ausentes")
        void findUsersTreatsMissingFieldsAsDisabledWithoutCreationDate() {

            // Arrange
            // Keycloak puede omitir campos; "enabled" ausente no debe contar como habilitado
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21"))
                    .andRespond(withSuccess("""
                            [{ "id": "u-2", "username": "sin.datos" }]
                            """, MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsers(null, null, 0, 21);

            // Assert
            assertThat(result).containsExactly(new IdentityUser(
                    "u-2", "sin.datos", null, null, null, false, null));
        }

        @Test
        @DisplayName("Debe devolver una lista vacía cuando la respuesta no tiene cuerpo")
        void findUsersReturnsAnEmptyListWhenTheBodyIsEmpty() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21"))
                    .andRespond(withSuccess());

            // Act
            List<IdentityUser> result = client.findUsers(null, null, 0, 21);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe traducir un error del servidor a IdentityProviderException conservando la causa")
        void findUsersTranslatesAServerErrorToAnIdentityProviderException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21"))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.findUsers(null, null, 0, 21));

            // Assert
            assertThat(thrown)
                    .isInstanceOf(IdentityProviderException.class)
                    .hasCauseInstanceOf(RestClientException.class);
        }

        @Test
        @DisplayName("Debe traducir un 403 por permisos faltantes a IdentityProviderException")
        void findUsersTranslatesMissingPermissionsToAnIdentityProviderException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users?first=0&max=21"))
                    .andRespond(withForbiddenRequest());

            // Act
            Throwable thrown = catchThrowable(() -> client.findUsers(null, null, 0, 21));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }

    @Nested
    @DisplayName("findUsersByRole()")
    class FindUsersByRoleTests {

        @Test
        @DisplayName("Debe pedir hasta 1000 miembros del rol en una sola llamada")
        void findUsersByRoleRequestsTheMembersOfTheRole() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/roles/ADMIN/users?first=0&max=1000"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            [{ "id": "u-1", "username": "juan", "enabled": true }]
                            """, MediaType.APPLICATION_JSON));

            // Act
            List<IdentityUser> result = client.findUsersByRole(UserRole.ADMIN);

            // Assert
            assertThat(result).extracting(IdentityUser::id).containsExactly("u-1");
        }

    }

    @Nested
    @DisplayName("findUserById()")
    class FindUserByIdTests {

        @Test
        @DisplayName("Debe devolver el usuario con sus datos convertidos")
        void findUserByIdMapsTheUser() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            { "id": "u-1", "username": "juan", "enabled": true }
                            """, MediaType.APPLICATION_JSON));

            // Act
            IdentityUser result = client.findUserById("u-1");

            // Assert
            assertThat(result.id()).isEqualTo("u-1");
            assertThat(result.enabled()).isTrue();
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando Keycloak response 404")
        void findUserByIdThrowsNotFoundWhenKeycloakRepliesWith404() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/ghost"))
                    .andRespond(withResourceNotFound());

            // Act
            Throwable thrown = catchThrowable(() -> client.findUserById("ghost"));

            // Assert
            assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Debe traducir otros errores a IdentityProviderException")
        void findUserByIdTranslatesOtherErrorsToAnIdentityProviderException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.findUserById("u-1"));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }

    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Debe enviar los datos con contraseña temporal y devolver el id del encabezado Location")
        void createUserSendsTheDataWithATemporaryPasswordAndReturnsTheIdFromTheLocation() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(jsonPath("$.username").value("juan.perez"))
                    .andExpect(jsonPath("$.email").value("juan@mail.com"))
                    .andExpect(jsonPath("$.firstName").value("Juan"))
                    .andExpect(jsonPath("$.lastName").value("Perez"))
                    .andExpect(jsonPath("$.enabled").value(true))
                    .andExpect(jsonPath("$.emailVerified").value(true))
                    .andExpect(jsonPath("$.credentials[0].type").value("password"))
                    .andExpect(jsonPath("$.credentials[0].value").value("Clave1234"))
                    .andExpect(jsonPath("$.credentials[0].temporary").value(true))
                    .andRespond(withCreatedEntity(URI.create(BASE_URL + "/users/new-id-123")));

            NewIdentityUser user = new NewIdentityUser("juan.perez","juan@mail.com","Juan","Perez");

            // Act
            String id = client.createUser(user, "Clave1234");

            // Assert
            assertThat(id).isEqualTo("new-id-123");

        }

        @Test
        @DisplayName("Debe traducir un 409 (usuario o correo repetido) a BusinessRuleException")
        void createUserTranslatesAConflictToABusinessRuleException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users"))
                    .andRespond(withRequestConflict());

            // Act
            Throwable thrown = catchThrowable(() -> client.createUser(newUser(), "Clave1234"));

            // Assert
            assertThat(thrown)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Ya existe un usuario con ese nombre de usuario o correo.");

        }

        @Test
        @DisplayName("Debe traducir un 400 (dato invalido o contraseña dèbil) a BusinessRuleException")
        void createUserTranslatesABadRequestToABusinessRuleException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users"))
                    .andRespond(withBadRequest());

            // Act
            Throwable thrown = catchThrowable(() -> client.createUser(newUser(), "Clave1234"));

            // Assert
            assertThat(thrown)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("No se pudo crear el usuario: la contraseña no cumple con la política de seguridad o algún dato no es válido.");

        }

        @Test
        @DisplayName("Debe fallar si Keycloak crea el usuario pero no informa dónde (sin Location)")
        void createUserFailsWhenKeycloakDoesNotReturnTheLocation() {

            // Arrange: respuesta 201 sin el encabezado Location
            server.expect(requestTo(BASE_URL + "/users"))
                    .andRespond(withStatus(HttpStatus.CREATED));

            // Act
            Throwable thrown = catchThrowable(() -> client.createUser(newUser(), "Clave1234"));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

        @Test
        @DisplayName("Debe traducir un error del servidor a IdentityProviderException")
        void createUserTranslatesAServerErrorToAnIdentityProviderException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users"))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.createUser(newUser(), "Clave1234"));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }

    @Nested
    @DisplayName("assignRole()")
    class AssignRoleTests {

        @Test
        @DisplayName("Debe consultar el rol y agregarlo al usuario")
        void assignRoleLooksUpTheRoleAndAddsItToTheUser() {

            // Arrange
            // Keycloak pide el rol completo (id y nombre), por eso primero se consulta
            expectRoleLookup("USER", "r-user");
            expectRoleMapping(HttpMethod.POST,"u-1","USER","r-user");

            // Act
            Throwable thrown = catchThrowable(() -> client.assignRole("u-1", UserRole.USER));

            // Assert
            assertThat(thrown).isNull();
            server.verify();
        }

        @Test
        @DisplayName("Debe traducir a IdentityProviderException que el rol no exista en el realm")
        void assignRoleTranslatesAMissingRoleToAnIdentityProviderException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/roles/USER"))
                    .andRespond(withResourceNotFound());

            // Act
            Throwable thrown = catchThrowable(() -> client.assignRole("u-1", UserRole.USER));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

        @Test
        @DisplayName("Debe traducir a IdentityProviderException un fallo al agregar el rol")
        void assignRoleTranslatesAFailureWhenAddingTheRole() {

            // Arrange
            expectRoleLookup("USER","r-user");
            server.expect(requestTo(BASE_URL + "/users/u-1/role-mappings/realm"))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.assignRole("u-1", UserRole.USER));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }

    @Nested
    @DisplayName("changeRole()")
    class ChangeRoleTests {

        @Test
        @DisplayName("Debe agregar el rol ADMIN y recién después quitar el rol USER")
        void changeRoleToAdminAddsAdminBeforeRemovingUser() {

            // Arrange
            expectRoleLookup("ADMIN","r-admin");
            expectRoleMapping(HttpMethod.POST,"u-1","ADMIN","r-admin");
            expectRoleLookup("USER","r-user");
            expectRoleMapping(HttpMethod.DELETE,"u-1","USER","r-user");

            // Act
            Throwable thrown = catchThrowable(() -> client.changeRole("u-1", UserRole.ADMIN));

            // Assert
            assertThat(thrown).isNull();
            server.verify();
        }

        @Test
        @DisplayName("Debe agregar el rol USER y recién después quitar el rol ADMIN")
        void changeRoleToUserAddsUserBeforeRemovingAdmin() {

            // Arrange
            expectRoleLookup("USER","r-user");
            expectRoleMapping(HttpMethod.POST,"u-1","USER","r-user");
            expectRoleLookup("ADMIN","r-admin");
            expectRoleMapping(HttpMethod.DELETE,"u-1","ADMIN","r-admin");

            // Act
            Throwable thrown = catchThrowable(() -> client.changeRole("u-1", UserRole.USER));

            // Assert
            assertThat(thrown).isNull();
            server.verify();
        }

        @Test
        @DisplayName("No debe quitar ningún rol cuando falla el alta del rol nuevo")
        void changeRoleDoesNotRemoveAnythingWhenAddingTheNewRoleFails() {

            // Arrange: solo se espera la consulta y el alta fallida.
            expectRoleLookup("ADMIN","r-admin");
            server.expect(requestTo(BASE_URL + "/users/u-1/role-mappings/realm"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.changeRole("u-1", UserRole.ADMIN));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }

    @Nested
    @DisplayName("setEnabled()")
    class SetEnabledTests {

        @Test
        @DisplayName("Debe reenviar el usuario completo con el estado deshabilitado")
        void setEnabledSendsTheWholeUserWithTheAccountDisabled() {

            // Arrange
            // Se lee el usuario y se vuelve a enviar entero para no depender de cómo Keycloak trate los campos omitidos
            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                              "id": "u-1",
                              "username": "juan",
                              "email": "juan@mail.com",
                              "enabled": true,
                              "attributes": { "telefono": ["123"] }
                            }
                            """, MediaType.APPLICATION_JSON));

            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.PUT))
                    .andExpect(jsonPath("$.enabled").value(false))
                    .andExpect(jsonPath("$.username").value("juan"))
                    .andExpect(jsonPath("$.email").value("juan@mail.com"))
                    .andExpect(jsonPath("$.attributes.telefono[0]").value("123"))
                    .andRespond(withNoContent());

            // Act
            Throwable thrown = catchThrowable(() -> client.setEnabled("u-1", false));

            // Assert
            assertThat(thrown).isNull();
            server.verify();
        }

        @Test
        @DisplayName("Debe reenviar el usuario completo con el estado habilitado")
        void setEnabledSendsTheWholeUserWithTheAccountEnabled() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            { "id": "u-1", "username": "juan", "enabled": false }
                            """, MediaType.APPLICATION_JSON));

            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.PUT))
                    .andExpect(jsonPath("$.enabled").value(true))
                    .andExpect(jsonPath("$.username").value("juan"))
                    .andRespond(withNoContent());

            // Act
            Throwable thrown = catchThrowable(() -> client.setEnabled("u-1", true));

            // Assert
            assertThat(thrown).isNull();
            server.verify();
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException y no actualizar nada si el usuario no existe")
        void setEnabledThrowsNotFoundWhenTheUserDoesNotExist() {

            // Arrange: solo se espera la lectura
            server.expect(requestTo(BASE_URL + "/users/ghost"))
                    .andRespond(withResourceNotFound());

            // Act
            Throwable thrown = catchThrowable(() -> client.setEnabled("ghost", false));

            // Assert
            assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Debe traducir a IdentityProviderException si fallo al actualizar")
        void setEnabledTranslatesAFailureWhenUpdating() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            { "id": "u-1", "username": "juan", "enabled": true }
                            """, MediaType.APPLICATION_JSON));

            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.setEnabled("u-1", false));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }

    @Nested
    @DisplayName("closeSessions()")
    class CloseSessionsTests {

        @Test
        @DisplayName("Debe pedir el cierre de sesiones del usuario")
        void closeSessionsRequestsTheLogoutOfTheUser() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1/logout"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withNoContent());

            // Act
            Throwable thrown = catchThrowable(() -> client.closeSessions("u-1"));

            // Assert
            assertThat(thrown).isNull();
            server.verify();
        }

        @Test
        @DisplayName("Debe traducir un error a IdentityProviderException")
        void closeSessionsTranslatesAnErrorToAnIdentityProviderException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1/logout"))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.closeSessions("u-1"));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("Debe borrar el usuario")
        void deleteUserDeletesTheUser() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withNoContent());

            // Act
            Throwable thrown = catchThrowable(() -> client.deleteUser("u-1"));

            // Assert
            assertThat(thrown).isNull();
            server.verify();
        }

        @Test
        @DisplayName("Debe traducir un error a IdentityProviderException")
        void deleteUserTranslatesAnErrorToAnIdentityProviderException() {

            // Arrange
            server.expect(requestTo(BASE_URL + "/users/u-1"))
                    .andRespond(withServerError());

            // Act
            Throwable thrown = catchThrowable(() -> client.deleteUser("u-1"));

            // Assert
            assertThat(thrown).isInstanceOf(IdentityProviderException.class);
        }

    }


    // ====== Helpers ======

    private static NewIdentityUser newUser() {
        return new NewIdentityUser("juan.perez","juan@mail.com","Juan", "Perez");
    }

    // Simula la consulta de un rol: Keycloak necesita su id para poder agregarlo o quitarlo
    private void expectRoleLookup(String roleName, String roleId) {
        server.expect(requestTo(BASE_URL + "/roles/" + roleName))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{ \"id\": \"" + roleId + "\", \"name\": \"" + roleName + "\" }"
                        , MediaType.APPLICATION_JSON
                ));
    }

    // Simula agregar (POST) o quitar (DELETE) un rol del usuario, y comprueba que rol se envia
    private void expectRoleMapping(HttpMethod httpMethod, String userId, String roleName, String roleId) {
        server.expect(requestTo(BASE_URL + "/users/" + userId + "/role-mappings/realm"))
                .andExpect(method(httpMethod))
                .andExpect(jsonPath("$[0].id").value(roleId))
                .andExpect(jsonPath("$[0].name").value(roleName))
                .andRespond(withNoContent());
    }

}

