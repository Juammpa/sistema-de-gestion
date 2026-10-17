package org.sistemagestion.backend.service.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.user.UserCreateRequestDto;
import org.sistemagestion.backend.dto.user.UserResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.IdentityProviderException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.model.enums.UserRole;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {

    @Mock
    private IdentityProviderClient identityProvider;

    @InjectMocks
    private UserAdminService service;

    @Nested
    @DisplayName("getUsers()")
    class GetUsersTests {

        @Test
        void getUsersResolvesRolesWithAdminTakingPrecedence() {

            // Arrange
            IdentityUser hasBothRoles = identityUser("u-both", true);
            IdentityUser onlyUser = identityUser("u-user", true);
            IdentityUser noRole = identityUser("u-none", true);

            when(identityProvider.findUsers(null, null, 0, 21))
                    .thenReturn(List.of(hasBothRoles, onlyUser, noRole));

            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of(hasBothRoles));
            when(identityProvider.findUsersByRole(UserRole.USER)).thenReturn(List.of(hasBothRoles, onlyUser));

            // Act
            SliceResponseDto<UserResponseDto> result = service.getUsers(null, null, 0, 20);

            // Assert
            assertThat(result.content())
                    .extracting(UserResponseDto::id, UserResponseDto::role)
                    .containsExactly(
                            tuple("u-both", UserRole.ADMIN),
                            tuple("u-user", UserRole.USER),
                            tuple("u-none", null));

            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void getUsersReportsANextPageWhenThereAreMoreResults() {

            // Arrange
            when(identityProvider.findUsers(null, null, 0, 3))
                    .thenReturn(List.of(identityUser("u1", true), identityUser("u2", true), identityUser("u3", true)));
            when(identityProvider.findUsersByRole(any())).thenReturn(List.of());

            // Act
            SliceResponseDto<UserResponseDto> result = service.getUsers(null, null, 0, 2);

            // Assert
            assertThat(result.content())
                    .extracting(UserResponseDto::id)
                    .containsExactly("u1","u2");

            assertThat(result.hasNext()).isTrue();
        }

        @Test
        void getUsersCorrectsAndOutOfRangePageAndSize() {

            // Arrange
            when(identityProvider.findUsers(null, null, 0, 101))
                    .thenReturn(List.of());
            when(identityProvider.findUsersByRole(any())).thenReturn(List.of());

            // Act
            SliceResponseDto<UserResponseDto> result = service.getUsers(null, null, -5, 1000);

            // Assert
            assertThat(result.page()).isZero();
            verify(identityProvider).findUsers(null, null, 0, 101);
        }

        @Test
        void getUsersCapsThePageNumberSoTheOffsetCannotOverflow() {

            // Arrange
            when(identityProvider.findUsers(null, null, 1_000_000, 101))
                    .thenReturn(List.of());
            when(identityProvider.findUsersByRole(any())).thenReturn(List.of());

            // Act
            SliceResponseDto<UserResponseDto> result = service.getUsers(null, null, Integer.MAX_VALUE, 100);

            // Assert
            assertThat(result.page()).isEqualTo(10_000);
            assertThat(result.content()).isEmpty();
        }

        @Test
        void getUsersPassesTheSearchTextAndStatusFilterToTheProvider() {

            // Arrange
            when(identityProvider.findUsers("prueba", false, 20, 21))
                    .thenReturn(List.of(identityUser("u1", false)));
            when(identityProvider.findUsersByRole(any())).thenReturn(List.of());

            // Act
            SliceResponseDto<UserResponseDto> result = service.getUsers("prueba", false, 1, 20);

            // Assert
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().enabled()).isFalse();

        }

    }

    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        void createUserTrimsTheFieldsAssignsTheRoleAndReturnsTheCreatedUser() {

            // Arrange
            UserCreateRequestDto request = new UserCreateRequestDto(
                    "juan.perez", "juan@mail.com", " Juan ",
                    " Perez ", UserRole.ADMIN, "ClaveTemporal1");

            when(identityProvider.createUser(new NewIdentityUser("juan.perez","juan@mail.com", "Juan", "Perez"), "ClaveTemporal1"))
                    .thenReturn("new-id");
            when(identityProvider.findUserById("new-id")).thenReturn(identityUser("new-id", true));

            // Act
            UserResponseDto result = service.createUser(request);

            // Assert
            verify(identityProvider).assignRole("new-id", UserRole.ADMIN);
            assertThat(result.id()).isEqualTo("new-id");
            assertThat(result.role()).isEqualTo(UserRole.ADMIN);

        }

        @Test
        void createUserRollsBackTheCreationWhenRoleAssignmentFails() {

            // Arrange
            IdentityProviderException failure = new IdentityProviderException("keycloak down", null);

            when(identityProvider.createUser(any(NewIdentityUser.class), eq("ClaveTemporal1")))
                    .thenReturn("new-id");
            doThrow(failure).when(identityProvider).assignRole("new-id", UserRole.USER);

            // Act
            Throwable thrown = catchThrowable(() -> service.createUser(validRequest(UserRole.USER)));

            // Assert
            assertThat(thrown).isSameAs(failure);
            verify(identityProvider).deleteUser("new-id");
            verify(identityProvider, never()).findUserById(any());
        }

        @Test
        void createUserKeepsTheOriginalErrorWhenTheRollbackAlsoFails() {

            // Arrange
            IdentityProviderException assignFailure = new IdentityProviderException("assign failed", null);
            IdentityProviderException deleteFailure = new IdentityProviderException("delete failed", null);

            when(identityProvider.createUser(any(NewIdentityUser.class), anyString()))
                    .thenReturn("new-id");
            doThrow(assignFailure).when(identityProvider).assignRole("new-id", UserRole.USER);
            doThrow(deleteFailure).when(identityProvider).deleteUser("new-id");

            // Act
            Throwable thrown = catchThrowable(() -> service. createUser(validRequest(UserRole.USER)));

            // Assert
            assertThat(thrown).isSameAs(assignFailure);
        }

        @Test
        void createUserDoesNotAssignARoleWhenTheCreationIsRejected() {

            // Arrange
            BusinessRuleException duplicate = new BusinessRuleException(
                    "Ya existe un usuario con ese nombre de usuario o correo.");

            when(identityProvider.createUser(any(NewIdentityUser.class), anyString()))
                    .thenThrow(duplicate);

            // Act
            Throwable thrown = catchThrowable(() -> service.createUser(validRequest(UserRole.USER)));

            // Assert
            assertThat(thrown).isSameAs(duplicate);
            verify(identityProvider, never()).assignRole(anyString(), any());
            verify(identityProvider, never()).deleteUser(anyString());
        }

    }

    @Nested
    @DisplayName("changeRol()")
    class ChangeRoleTests {

        @Test
        void changeRolRejectsChangingTheOwnRole() {

            // Arrange
            String actorId = "admin-1";

            // Act
            Throwable thrown = catchThrowable(() -> service.changeRole(actorId, UserRole.USER, actorId));

            // Assert
            assertThat(thrown)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("No podés cambiar tu propio rol.");

            verifyNoInteractions(identityProvider);
        }

        @Test
        void changeRoleRejectsDemotingTheLastEnabledAdmin() {

            // Arannge
            IdentityUser target = identityUser("admin-2", true);

            when(identityProvider.findUserById("admin-2")).thenReturn(target);
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of(target));

            // Act
            Throwable thrown = catchThrowable(() -> service.changeRole("admin-2", UserRole.USER, "admin-1"));

            // Assert
            assertThat(thrown)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("No se puede quitar el rol de administrador al último administrador habilitado.");
            verify(identityProvider, never()).changeRole(anyString(), any());
        }

        @Test
        void changeRoleDoesNotCountDisabledAdminisAsAnotherAdmin() {

            // Arrange
            IdentityUser target = identityUser("admin-2", true);
            IdentityUser disabledAdmin = identityUser("admin-3", false);

            when(identityProvider.findUserById("admin-2")).thenReturn(target);
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of(target, disabledAdmin));

            // Act
            Throwable thrown = catchThrowable(() -> service.changeRole("admin-2", UserRole.USER,"admin-1"));

            // Assert
            assertThat(thrown).isInstanceOf(BusinessRuleException.class);
            verify(identityProvider, never()).changeRole(anyString(), any());
        }

        @Test
        void changeRoleAllowsDemotingAnAdminWhenAnotherEnabledAdminRemains() {

            // Arrange
            IdentityUser target = identityUser("admin-2", true);
            IdentityUser anotherAdmin = identityUser("admin-3", true);

            when(identityProvider.findUserById("admin-2")).thenReturn(target);
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of(target, anotherAdmin));

            // Act
            UserResponseDto result = service.changeRole("admin-2", UserRole.USER,"admin-1");

            // Assert
            verify(identityProvider).changeRole("admin-2", UserRole.USER);
            assertThat(result.role()).isEqualTo(UserRole.USER);
        }

        @Test
        void changeRoleAllowsPromotingAUserToAdmin() {

            // Arrange
            IdentityUser target = identityUser("user-1", true);

            when(identityProvider.findUserById("user-1")).thenReturn(target);
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of(identityUser("admin-1", true)));

            // Act
            UserResponseDto result = service.changeRole("user-1", UserRole.ADMIN, "admin-1");

            // Assert
            verify(identityProvider).changeRole("user-1", UserRole.ADMIN);
            assertThat(result.role()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        void changeRoleFailsWhenTheUserDoesNotExist() {

            // Arrange
            ResourceNotFoundException notFound = new ResourceNotFoundException("User","id","ghost");

            when(identityProvider.findUserById("ghost")).thenThrow(notFound);

            // Act
            Throwable thrown = catchThrowable(() -> service.changeRole("ghost", UserRole.USER, "admin-1"));

            // Assert
            assertThat(thrown).isSameAs(notFound);
            verify(identityProvider, never()).changeRole(anyString(), any());
        }
    }

    @Nested
    @DisplayName("disableUser()")
    class DisableUserTests {

        @Test
        void disableUserRejectsDisablingTheOwnAccount() {

            // Arrange
            String actorId = "admin-1";

            // Act
            Throwable thrown = catchThrowable(() -> service.disableUser(actorId, actorId));

            // Assert
            assertThat(thrown)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("No podés deshabilitar tu propia cuenta.");
            verifyNoInteractions(identityProvider);
        }

        @Test
        void disableUserRejectsDisablingTheLastEnabledAdmin() {

            // Arrange
            IdentityUser target = identityUser("admin-2", true);

            when(identityProvider.findUserById("admin-2")).thenReturn(target);
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of(target));

            // Act
            Throwable thrown = catchThrowable(() -> service.disableUser("admin-2", "admin-1"));

            // Assert
            assertThat(thrown)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("No se puede deshabilitar al último administrador habilitado.");
            verify(identityProvider, never()).setEnabled(anyString(), anyBoolean());
            verify(identityProvider, never()).closeSessions(anyString());

        }

        @Test
        void disableUserAllowsDisablingAnAdminWhenAnotherEnabledAdminRemains() {

            // Arrange
            IdentityUser target = identityUser("admin-2", true);
            IdentityUser anotherAdmin = identityUser("admin-3", true);

            when(identityProvider.findUserById("admin-2")).thenReturn(target);
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of(target, anotherAdmin));

            // Act
            service.disableUser("admin-2", "admin-1");

            // Assert
            verify(identityProvider).setEnabled("admin-2", false);
        }

        @Test
        void disableUserDisablesTheAccountAndClosesItsSessions() {

            // Arrange
            when(identityProvider.findUserById("user-1")).thenReturn(identityUser("user-1", true));
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of());

            // Act
            service.disableUser("user-1", "admin-1");

            // Assert
            verify(identityProvider).setEnabled("user-1", false);
            verify(identityProvider).closeSessions("user-1");
        }

        @Test
        void disabledUserDoesNotNothingWhenTheAccountIsAlreadyDisabled() {

            // Arrange
            when(identityProvider.findUserById("user-1")).thenReturn(identityUser("user-1", false));

            // Act
            service.disableUser("user-1", "admin-1");

            // Assert
            verify(identityProvider, never()).setEnabled(anyString(), anyBoolean());
            verify(identityProvider, never()).closeSessions(anyString());
        }

        @Test
        void disableUserSucceedsEvenWhenClosingTheSessionsFails() {

            // Arrange
            when(identityProvider.findUserById("user-1")).thenReturn(identityUser("user-1", true));
            when(identityProvider.findUsersByRole(UserRole.ADMIN)).thenReturn(List.of());
            doThrow(new IdentityProviderException("keycloak down", null))
                    .when(identityProvider).closeSessions("user-1");

            // Act
            Throwable thrown = catchThrowable(() -> service.disableUser("user-1", "admin-1"));

            // Assert
            assertThat(thrown).isNull();
            verify(identityProvider).setEnabled("user-1", false);
        }
    }

    @Nested
    @DisplayName("activateUser()")
    class ActivateUserTests {

        @Test
        void activateUserEnablesTheAccount() {

            // Act
            service.activateUser("user-1");

            // Assert
            verify(identityProvider).setEnabled("user-1", true);
        }

        @Test
        void activateUserDoesNotSmallowAnErrorFromTheProvider() {

            // Arrange
            ResourceNotFoundException notFound = new ResourceNotFoundException("User","id","ghost");

            doThrow(notFound).when(identityProvider).setEnabled("ghost", true);

            // Act
            Throwable thrown = catchThrowable(() -> service.activateUser("ghost"));

            // Assert
            assertThat(thrown).isSameAs(notFound);
        }

    }


    // ====== Helpers ======

    private static IdentityUser identityUser(String id, boolean enabled) {
        return new IdentityUser(
                id, "user-" + id, id + "@mail.com", "First","Last", enabled, Instant.parse("2026-01-01T00:00:00Z"));
    }

    private static UserCreateRequestDto validRequest(UserRole role) {
        return new UserCreateRequestDto("juan.perez", "juan@mail.com", "Juan", "Perez",role, "ClaveTemporal1");
    }

}
