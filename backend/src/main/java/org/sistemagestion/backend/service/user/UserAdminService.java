package org.sistemagestion.backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.user.UserCreateRequestDto;
import org.sistemagestion.backend.dto.user.UserResponseDto;
import org.sistemagestion.backend.model.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminService implements IUserAdminService {

    private static final int MAX_PAGE = 10_000;
    private static final int MAX_PAGE_SIZE = 100;

    private final IdentityProviderClient identityProvider;

    @Override
    public SliceResponseDto<UserResponseDto> getUsers(String search, int page, int size) {

        int safePage = Math.min(Math.max(page, 0), MAX_PAGE);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        // Se pide uno de mas para saber si hay otra pagina, sin hacer conteo aparte
        List<IdentityUser> found = identityProvider.findUsers(search, safePage * safeSize, safeSize + 1);
        boolean hasNext = found.size() > safeSize;
        List<IdentityUser> pageUsers = hasNext ? found.subList(0, safeSize) : found;

        // Dos consultas para resolver el rol de todos, en vez de una por usuaro
        Set<String> adminIds = identityProvider.findUserIdsByRole(UserRole.ADMIN);
        Set<String> userIds = identityProvider.findUserIdsByRole(UserRole.USER);

        List<UserResponseDto> content = pageUsers.stream()
                .map(user -> toResponse(user, roleOf(user.id(), adminIds, userIds)))
                .toList();

        return new SliceResponseDto<>(content, safePage, hasNext);
    }

    @Override
    public UserResponseDto createUser(UserCreateRequestDto request) {

        String userId = identityProvider.createUser(
                new NewIdentityUser(
                        request.username().trim(),
                        request.email().trim(),
                        request.firstName().trim(),
                        request.lastName().trim()),
                request.temporaryPassword());

        try {
            identityProvider.assignRole(userId, request.role());
        } catch (RuntimeException ex) {
            // Sin rol el usuario no podria usar la API: se deshace la creacion
            rollbackCreation(userId);
            throw ex;
        }

        return toResponse(identityProvider.findUserById(userId), request.role());
    }

    // Mejor esfuerzo: si el borrado tambien falla, solo se registra para limpiarlo a mano
    private void rollbackCreation(String userId) {
        try {
            identityProvider.deleteUser(userId);
        } catch (RuntimeException ex) {
            log.error("No se pudo deshacer la creacion del usuario {}", userId, ex);
        }
    }

    // Si tiene los dos roles, manda ADMIN.
    // Sin ningun rol, devuelve null
    private UserRole roleOf(String userId, Set<String> adminIds, Set<String> userIds) {

        if (adminIds.contains(userId)) return UserRole.ADMIN;
        if (userIds.contains(userId)) return UserRole.USER;

        return null;
    }

    private UserResponseDto toResponse(IdentityUser user, UserRole role) {

        return new UserResponseDto(
                user.id(),
                user.username(),
                user.email(),
                user.firstName(),
                user.lastName(),
                user.enabled(),
                role,
                user.createdAt());
    }

}
