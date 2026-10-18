import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AccountProfile } from '../interfaces/account.interface';
import { ACCOUNT_URL } from '../../core/auth/keycloak-urls';

// Se pide JSON de forma explicita: Sin esto, Keycloak podria devolver la pagina HTML de su consola de cuenta
const JSON_ONLY = { headers: { Accept: 'application/json' } };

@Injectable({ providedIn: 'root' })
export class AccountService {
  private http = inject(HttpClient);

  // Perfil del usuario que inicio sesion
  getProfile() {
    return this.http.get<AccountProfile>(ACCOUNT_URL, JSON_ONLY);
  }

  // Guarda nombre y apellido.
  // Keycloak exige enviar tambien usuario y correo, sin cambios
  updateProfile(profile: AccountProfile) {
    const { username, email, firstName, lastName } = profile;

    return this.http.post<void>(ACCOUNT_URL, { username, email, firstName, lastName }, JSON_ONLY);
  }
}
