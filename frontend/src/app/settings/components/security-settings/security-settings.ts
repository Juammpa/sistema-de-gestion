import { Component, inject, signal } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { AccountActionService } from '../../../core/auth/account-action.service';

@Component({
  selector: 'security-settings',
  templateUrl: './security-settings.html',
  host: { class: 'block' },
})
export class SecuritySettings {
  private auth = inject(AuthService);
  private notifications = inject(NotificationService);

  protected readonly ui = SETTINGS_UI;
  protected readonly redirecting = signal<boolean>(false);

  // Datos de ejemplo: mas adelante vendran del backend
  protected readonly sessions = [
    {
      device: 'Chrome · Windows',
      icon: 'fa-solid fa-desktop',
      location: 'Concordia, Entre Ríos',
      lastActive: 'Activa ahora',
      current: true,
    },
    {
      device: 'App · iPhone',
      icon: 'fa-solid fa-mobile-screen',
      location: 'Concordia, Entre Ríos',
      lastActive: 'Hace 2 días',
      current: false,
    },
  ];

  constructor() {
    // Si el usuario vuelve de la pantalla de Keycloak, se le avisa como termino el cambio
    const result = inject(AccountActionService).consume('UPDATE_PASSWORD');

    if (result === 'success') this.notifications.success('Contraseña actualizada');

    if (result === 'error') {
      this.notifications.error('No se pudo actualizar la contraseña. Intentá de nuevo.');
    }
  }

  protected changePassword() {
    if (this.redirecting()) return;

    this.redirecting.set(true);

    // Si no se pudo redirigir, el boton vuelve a estar disponible
    this.auth.changePassword().catch(() => this.redirecting.set(false));
  }
}
