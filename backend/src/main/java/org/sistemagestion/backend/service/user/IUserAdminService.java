package org.sistemagestion.backend.service.user;

import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.user.UserCreateRequestDto;
import org.sistemagestion.backend.dto.user.UserResponseDto;

public interface IUserAdminService {

    SliceResponseDto<UserResponseDto> getUsers(String search, int page, int size);

    UserResponseDto createUser(UserCreateRequestDto request);
}
