import { Component, computed, input, output, signal } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';

@Component({
  selector: 'delete-account-modal',
  templateUrl: './delete-account-modal.html',
})
export class DeleteAccountModal {
  username = input.required<string>();
  loading = input<boolean>(false);

  cancel = output();
  confirm = output<string>(); // Lo que escribio el usuario

  protected readonly ui = SETTINGS_UI;
  protected typed = signal('');

  // Habilita el boton solo si lo escrito coincide con el usuario
  protected canConfirm = computed(
    () =>
      this.username().length > 0 &&
      this.typed().trim().toLowerCase() === this.username().toLowerCase(),
  );

  protected close() {
    if (!this.loading()) this.cancel.emit();
  }

  protected submit() {
    if (this.canConfirm() && !this.loading()) {
      this.confirm.emit(this.typed().trim());
    }
  }
}
