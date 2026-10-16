package org.sistemagestion.backend.service.user;

import org.sistemagestion.backend.model.enums.UserRole;

import java.util.List;

public interface IdentityProviderClient {

    List<IdentityUser> findUsers(String search, int first, int max);
    IdentityUser findUserById(String userId);
    List<IdentityUser> findUsersByRole(UserRole role);

    // Devuelve el id del usuario creado.
    // La contraseña es temporal (debe cambiarla al primer ingreso)
    String createUser(NewIdentityUser user, String temporaryPassword);

    void assignRole(String userId, UserRole role);
    void changeRole(String userId, UserRole role);

    // Solo para deshacer una creacion incompleta
    void deleteUser(String userId);

    // Habilita o deshabilita la cuenta (no la borra).
    void setEnabled(String userId, boolean enabled);

    // Cierra las sesiones abiertas del usuarios para que no pueda renovar su token
    void closeSessions(String userId);
}
