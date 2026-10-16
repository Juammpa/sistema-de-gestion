package org.sistemagestion.backend.service.user;

import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.user.UserCreateRequestDto;
import org.sistemagestion.backend.dto.user.UserResponseDto;
import org.sistemagestion.backend.model.enums.UserRole;

public interface IUserAdminService {

    SliceResponseDto<UserResponseDto> getUsers(String search, int page, int size);
    UserResponseDto createUser(UserCreateRequestDto request);
    // actorId es el id del admin que hace el pedido (sale del token)
    UserResponseDto changeRole(String userId, UserRole newRole, String actorId);

    // No borra: la cuenta queda deshabilitada y sus datos se conservan
    void disableUser(String userId, String actorId);

    void activateUser(String userId);
}
