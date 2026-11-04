package org.sistemagestion.backend.service.account;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.service.user.IUserAdminService;
import org.sistemagestion.backend.service.user.IdentityProviderClient;
import org.sistemagestion.backend.service.user.IdentityUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final IdentityProviderClient identityProvider;
    private final IUserAdminService userAdminService;
    private final OwnerDataPurger ownerDataPurger;

    // Primero los datos y despues el usuario de Keycloak: si Keycloak falla, la cuenta queda vacia pero se puede reintentar
    @Override
    public void deleteAccount(String userId, String confirmation) {

        // El nombre de usuario se lee de Keycloak, no del token
        IdentityUser user = identityProvider.findUserById(userId);

        if (!user.username().equalsIgnoreCase(confirmation.trim())) {
            throw new BusinessRuleException("El nombre de usuario no coincide.");
        }

        userAdminService.removeUser(userId, () -> ownerDataPurger.purge(UUID.fromString(userId)));
    }
}
