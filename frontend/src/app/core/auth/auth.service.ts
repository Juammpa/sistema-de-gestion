import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

// Unica puerta de entrada a la identidad del usuario.
// La app arranca con onLoad 'login-required', asi que aca siempre hay una sesion iniciada.
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = inject(Keycloak);

  readonly roles: readonly string[] = this.keycloak.realmAccess?.roles ?? [];
  readonly isAdmin = this.roles.includes('ADMIN');

  readonly username: string = this.keycloak.idTokenParsed?.['preferred_username'] ?? '';
  readonly email: string = this.keycloak.idTokenParsed?.['email'] ?? '';
  readonly fullName: string = this.keycloak.idTokenParsed?.['name'] ?? this.username;

  readonly initials: string =
    this.fullName
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((word) => word[0].toUpperCase())
      .join('') || '?';

  readonly roleLabel = this.isAdmin ? 'Administrador' : 'Usuario';

  logout(): Promise<void> {
    // La barra final es necesaria para coincidir con la URI de "post logout" configurada en Keycloak
    return this.keycloak.logout({
      redirectUri: `${window.location.origin}/`,
    });
  }
}
