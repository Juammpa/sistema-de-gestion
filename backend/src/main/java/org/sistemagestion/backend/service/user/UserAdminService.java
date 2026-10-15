package org.sistemagestion.backend.service.user;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.user.UserResponseDto;
import org.sistemagestion.backend.model.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
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
