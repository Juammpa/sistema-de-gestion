import { Component, effect, inject, signal } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';
import { AccountService } from '../../services/account.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';
import { FormUtils } from '../../../shared/utils/form-utils';
import { rxResource } from '@angular/core/rxjs-interop';
import { AppValidators } from '../../../shared/validators/app-validators';
import { ErrorState } from '../../../shared/components/error-state/error-state';

// Mismas reglas para nombre y apellido
const NAME_VALIDATORS = [
  Validators.required,
  AppValidators.noBlank,
  AppValidators.trimmedLength(1, 50),
  AppValidators.personName,
];

@Component({
  selector: 'profile-settings',
  imports: [ReactiveFormsModule, ErrorState],
  templateUrl: './profile-settings.html',
  host: { class: 'block' },
})
export class ProfileSettings {
  private accountService = inject(AccountService);
  private notifications = inject(NotificationService);
  private fb = inject(FormBuilder);

  protected readonly auth = inject(AuthService);
  protected readonly ui = SETTINGS_UI;
  protected readonly formUtils = FormUtils;

  // Perfil real, leido de Keycloak
  protected readonly profile = rxResource({
    stream: () => this.accountService.getProfile(),
  });

  // Solo nombre y apellido son editables
  protected readonly form = this.fb.nonNullable.group({
    firstName: ['', NAME_VALIDATORS],
    lastName: ['', NAME_VALIDATORS],
  });

  protected readonly saving = signal<boolean>(false);

  constructor() {
    // Cuando llega el perfil (o se vuelve a leer), se carga el formulario
    effect(() => {
      if (this.profile.hasValue()) this.restore();
    });
  }

  // Vuelve el formulario a lo que esta guardado en Keycloak
  protected restore() {
    const profile = this.profile.value();
    if (!profile) return;

    this.form.reset({ firstName: profile.firstName ?? '', lastName: profile.lastName ?? '' });
  }

  protected save() {
    const current = this.profile.value();

    if (this.form.invalid || !current) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.saving()) return;

    const { firstName, lastName } = this.formUtils.trimValues(this.form.getRawValue());

    this.saving.set(true);

    this.accountService.updateProfile({ ...current, firstName, lastName }).subscribe({
      next: async () => {
        this.notifications.success('Perfil actualizado');

        // Se renueva el token para que el menu lateral muestre el nombre nuevo sin recargar la pagina
        try {
          await this.auth.refreshProfile();
        } catch {
          // No se avisa: es un detalle de pantalla que se corrige solo
        } finally {
          this.profile.reload();
          this.saving.set(false);
        }
      },
      error: () => this.saving.set(false), // El aviso lo muestra el interceptor
    });
  }
}
