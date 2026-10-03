import { Component, inject, signal } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';
import { ExportService } from '../../services/export.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'account-settings',
  templateUrl: './account-settings.html',
  host: { class: 'block' },
})
export class AccountSettings {
  private exportService = inject(ExportService);
  private notifications = inject(NotificationService);

  protected readonly ui = SETTINGS_UI;
  protected exporting = signal(false);

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
}
