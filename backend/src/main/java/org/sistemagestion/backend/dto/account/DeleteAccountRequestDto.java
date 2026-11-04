package org.sistemagestion.backend.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Cuerpo para eliminar la propia cuenta
public record DeleteAccountRequestDto (

        @NotBlank(message = "Escribí tu nombre de usuario para confirmar")
        @Size(max = 100, message = "El nombre de usuario es demasiado largo")
        String confirmation
) {}
