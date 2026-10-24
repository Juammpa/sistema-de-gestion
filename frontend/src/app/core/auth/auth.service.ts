import { computed, inject, Injectable, signal } from '@angular/core';
import Keycloak from 'keycloak-js';

// Unica puerta de entrada a la identidad del usuario
// Solo se usa dentro del panel (/home), que esta protegido por authGuard: ahi siempre hay sesion
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = inject(Keycloak);

  // Estos datos no cambian mientras dura la sesion
  readonly roles: readonly string[] = this.keycloak.realmAccess?.roles ?? [];
  readonly isAdmin = this.roles.includes('ADMIN');
  readonly roleLabel = this.isAdmin ? 'Administrador' : 'Usuario';

  readonly userId: string = this.keycloak.idTokenParsed?.sub ?? '';
  readonly username: string = this.keycloak.idTokenParsed?.['preferred_username'] ?? '';

  // Nombre y correo si pueden cambiar: el usuario edita su perfil en Ajustes
  private readonly claims = signal(this.readClaims());

  readonly email = computed(() => this.claims().email);
  readonly fullName = computed(() => this.claims().name || this.username);

  readonly initials = computed(
    () =>
      this.fullName()
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((word) => word[0].toUpperCase())
        .join('') || '?',
  );

  // Pide un token nuevo (el -1 fuerza la renovacion) para que el nombre y el correo
  // reflejen un cambio reciente del perfil sin tener que cerrar sesion.
  async refreshProfile(): Promise<void> {
    await this.keycloak.updateToken(-1);
    this.claims.set(this.readClaims());
  }

  logout(): Promise<void> {
    // La barra final es necesaria para coincidir con la URI de "post logout" configurada en Keycloak
    return this.keycloak.logout({
      redirectUri: `${window.location.origin}/`,
    });
  }

  private readClaims() {
    const token = this.keycloak.idTokenParsed;

    return {
      name: (token?.['name'] as string | undefined) ?? '',
      email: (token?.['email'] as string | undefined) ?? '',
    };
  }

  // Lleva al usuario a la pantalla de Keycloak para cambiar la contraseña
  changePassword(): Promise<void> {
    return this.keycloak.login({
      action: 'UPDATE_PASSWORD',
      redirectUri: `${window.location.origin}/home/ajustes?seccion=seguridad`,
    });
  }
}
