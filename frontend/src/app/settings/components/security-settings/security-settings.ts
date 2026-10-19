import { Component, computed, inject, signal } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { AccountActionService } from '../../../core/auth/account-action.service';
import { AccountDevice, AccountSession } from '../../interfaces/account.interface';
import { DatePipe } from '@angular/common';
import { ErrorState } from '../../../shared/components/error-state/error-state';
import { AccountService } from '../../services/account.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

// Una sesion abierta junto con los datos del dispositivo (navegador y sistema) desde el que se inicio
interface SessionRow {
  session: AccountSession;
  device: AccountDevice;
}

@Component({
  selector: 'security-settings',
  imports: [DatePipe, ErrorState],
  templateUrl: './security-settings.html',
  host: { class: 'block' },
})
export class SecuritySettings {
  private auth = inject(AuthService);
  private accountService = inject(AccountService);
  private notifications = inject(NotificationService);

  protected readonly ui = SETTINGS_UI;
  protected readonly redirecting = signal<boolean>(false);

  // Dispositivos con sesion abierta, leidos de Keycloak
  protected readonly devices = rxResource({
    stream: () => this.accountService.getDevices(),
  });

  // Un renglon por sesion (no por dispositivo): la actual primero
  protected readonly rows = computed<SessionRow[]>(() =>
    (this.devices.hasValue() ? this.devices.value() : [])
      .flatMap((device) => device.sessions.map((session) => ({ session, device })))
      .sort(
        (a, b) =>
          Number(!!b.session.current) - Number(!!a.session.current) ||
          b.session.lastAccess - a.session.lastAccess,
      ),
  );

  protected readonly otherSessionsCount = computed(
    () => this.rows().filter((row) => !row.session.current).length,
  );

  // Estado de las acciones en curso
  protected readonly closingSessionId = signal<string | null>(null);
  protected readonly closingOthers = signal<boolean>(false);
  protected readonly confirmingCloseOthers = signal<boolean>(false);
  protected readonly busy = computed(
    () => this.closingSessionId() !== null || this.closingOthers(),
  );

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

  // Nombre legible del dispositivo (ej. Chrome · Windows )
  protected deviceName(device: AccountDevice): string {
    // Keycloak informa el navegador con su version ("Chrome/141.0.0")
    const browser = device.browser?.split('/')[0];

    // "Other" es lo que informa Keycloak cuando no reconoce el SO
    const system = device.os === 'Other' ? null : device.os;

    return [browser, system].filter(Boolean).join(' · ') || 'Dispositivo desconocido';
  }

  protected closeSession(row: SessionRow) {
    if (this.busy() || row.session.current) return;

    this.closingSessionId.set(row.session.id);

    this.accountService
      .deleteSession(row.session.id)
      .pipe(finalize(() => this.closingSessionId.set(null)))
      .subscribe({
        next: () => {
          this.notifications.success('Sesión cerrada');
          this.devices.reload();
        },
        error: () => this.devices.reload(),
      });
  }

  protected closeOtherSessions() {
    if (this.busy()) return;

    this.closingOthers.set(true);

    this.accountService
      .deleteOtherSessions()
      .pipe(
        finalize(() => {
          this.closingOthers.set(false);
          this.confirmingCloseOthers.set(false);
        }),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Se cerraron las demás sesiones');
          this.devices.reload();
        },
        error: () => this.devices.reload(),
      });
  }
}
