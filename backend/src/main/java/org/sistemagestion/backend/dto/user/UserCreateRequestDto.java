package org.sistemagestion.backend.dto.user;

import jakarta.validation.constraints.*;
import org.sistemagestion.backend.model.enums.UserRole;

public record UserCreateRequestDto(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "Solo se permiten letras, números, puntos, guiones y guion bajo")
        String username,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String email,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido no puede superar los 50 caracteres")
        String lastName,

        @NotNull(message = "El rol es obligatorio")
        UserRole role,

        @NotBlank(message = "La contraseña temporal es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        String temporaryPassword

) {

    // Nunca debe aparecer la contraseña en un log
    @Override
    public String toString() {
        return "UserCreateRequestDto[username=" + username + ", role=" + role + "]";
    }

}
