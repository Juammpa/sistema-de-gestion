package org.sistemagestion.backend.service.user;

import org.sistemagestion.backend.model.enums.UserRole;

import java.util.List;
import java.util.Set;

public interface IdentityProviderClient {

    List<IdentityUser> findUsers(String search, int first, int max);
    Set<String> findUserIdsByRole(UserRole role);

}
