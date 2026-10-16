package org.sistemagestion.backend.dto.user;

import jakarta.validation.constraints.NotNull;
import org.sistemagestion.backend.model.enums.UserRole;

// Cuerpo para cambiar el rol de un usuario
public record UserRoleRequestDto(

        @NotNull(message = "El rol es obligatorio")
        UserRole role
) {}
