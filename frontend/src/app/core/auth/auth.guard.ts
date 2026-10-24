import { CanActivateFn } from '@angular/router';
import { createAuthGuard } from 'keycloak-angular';

// Deja entrar al panel solo con sesion iniciada. Si no hay, lleva al login y vuelve a la misma pagina
export const authGuard = createAuthGuard<CanActivateFn>(
  async (_route, state, { authenticated, keycloak }) => {
    if (authenticated) return true;

    await keycloak.login({ redirectUri: `${window.location.origin}${state.url}` });
    return false;
  },
);
