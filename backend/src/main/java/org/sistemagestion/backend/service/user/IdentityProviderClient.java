package org.sistemagestion.backend.service.user;

import org.sistemagestion.backend.model.enums.UserRole;

import java.util.List;
import java.util.Set;

public interface IdentityProviderClient {

    List<IdentityUser> findUsers(String search, int first, int max);
    Set<String> findUserIdsByRole(UserRole role);
    IdentityUser findUserById(String userId);

    // Devuelve el id del usuario creado.
    // La contraseña es temporal (debe cambiarla al primer ingreso)
    String createUser(NewIdentityUser user, String temporaryPassword);

    void assignRole(String userId, UserRole role);

    // Solo para deshacer una creacion incompleta
    void deleteUser(String userId);
}
