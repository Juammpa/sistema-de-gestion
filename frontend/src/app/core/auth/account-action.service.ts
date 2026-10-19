import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

export type ActionStatus = 'success' | 'cancelled' | 'error';

// Recuerda como termino una accion hecha en Keycloak (ej. cambiar la contraseña)
@Injectable({ providedIn: 'root' })
export class AccountActionService {
  private readonly keycloak = inject(Keycloak);
  private result: { action: string; status: ActionStatus } | null = null;

  constructor() {
    // keycloak-angular ya usa este aviso, asi que se conserva el anterior
    const previous = this.keycloak.onActionUpdate;

    this.keycloak.onActionUpdate = (status, action) => {
      previous?.call(this.keycloak, status, action);
      this.result = { action: action ?? '', status };
    };
  }

  // Devuelve el resultado de esa accion una sola vez (despues se olvida) o null si no hubo
  consume(action: string): ActionStatus | null {
    if (this.result?.action !== action) return null;

    const { status } = this.result;
    this.result = null;

    return status;
  }
}
