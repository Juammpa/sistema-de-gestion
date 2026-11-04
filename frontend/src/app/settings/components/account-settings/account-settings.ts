import { Component, inject, signal } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';
import { ExportService } from '../../services/export.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { finalize } from 'rxjs';
import { AccountDeletionService } from '../../services/account-deletion.service';
import { AuthService } from '../../../core/auth/auth.service';
import { DeleteAccountModal } from '../delete-account-modal/delete-account-modal';

@Component({
  selector: 'account-settings',
  imports: [DeleteAccountModal],
  templateUrl: './account-settings.html',
  host: { class: 'block' },
})
export class AccountSettings {
  private exportService = inject(ExportService);
  private accountDeletion = inject(AccountDeletionService);
  private notifications = inject(NotificationService);

  protected readonly auth = inject(AuthService);
  protected readonly ui = SETTINGS_UI;
  protected exporting = signal(false);

  protected showDeleteModal = signal(false);
  protected deleting = signal(false);

  protected requestExport() {
    if (this.exporting()) return;
    this.exporting.set(true);

    this.exportService
      .export()
      .pipe(finalize(() => this.exporting.set(false)))
      .subscribe({
        next: (fileName) => this.notifications.success(`Exportación lista: ${fileName}`),
        // El interceptor ya mostro el aviso de error
        error: () => {},
      });
  }

  protected deleteAccount(confirmation: string) {
    if (this.deleting()) return;
    this.deleting.set(true);

    this.accountDeletion
      .deleteAccount(confirmation)
      .pipe(finalize(() => this.deleting.set(false)))
      .subscribe({
        // La cuenta ya no existe: se cierra la sesion y se vuelve al inicio
        next: () => void this.auth.logout(),
        error: () => {}, // El interceptor mostro el aviso de error
      });
  }
}
