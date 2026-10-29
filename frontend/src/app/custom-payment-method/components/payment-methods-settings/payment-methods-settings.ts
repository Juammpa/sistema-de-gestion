import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { rxResource } from '@angular/core/rxjs-interop';
import { finalize, Observable, of } from 'rxjs';
import { AppValidators } from '../../../shared/validators/app-validators';
import { uniqueValue } from '../../../shared/validators/async-validators';
import { ErrorState } from '../../../shared/components/error-state/error-state';
import { FormUtils } from '../../../shared/utils/form-utils';
import { NotificationService } from '../../../core/notifications/notification.service';
import { CustomPaymentMethodService } from '../../../custom-payment-method/services/custom-payment-method.service';
import { CustomPaymentMethod } from '../../../custom-payment-method/interfaces/custom-payment-method.interface';
import { SETTINGS_UI } from '../../../settings/utils/settings-ui';

const NAME_VALIDATORS = [
  Validators.required,
  AppValidators.noBlank,
  AppValidators.trimmedLength(2, 60),
];

const DUPLICATED_ENTITY = 'un método de pago con ese nombre';

const ICON_BUTTON =
  'flex size-10 shrink-0 cursor-pointer items-center justify-center rounded-lg text-slate-400 transition-colors duration-150 hover:bg-slate-800/70 hover:text-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-400/70 disabled:cursor-not-allowed disabled:opacity-50';

@Component({
  selector: 'payment-methods-settings',
  imports: [ReactiveFormsModule, ErrorState],
  templateUrl: './payment-methods-settings.html',
})
export class PaymentMethodsSettings {
  private methodService = inject(CustomPaymentMethodService);
  private notifications = inject(NotificationService);
  private fb = inject(FormBuilder);

  protected readonly ui = SETTINGS_UI;
  protected readonly formUtils = FormUtils;
  protected readonly iconButton = ICON_BUTTON;
  protected readonly dangerIconButton = `${ICON_BUTTON} hover:!text-red-300`;

  protected readonly methods = rxResource({
    stream: () => this.methodService.getAll(),
  });

  protected readonly active = computed(() =>
    (this.methods.hasValue() ? this.methods.value() : []).filter((method) => method.isActive),
  );

  protected readonly archived = computed(() =>
    (this.methods.hasValue() ? this.methods.value() : []).filter((method) => !method.isActive),
  );

  // Evita acciones superpuestas (guardar, eliminar, reactivar)
  protected readonly saving = signal(false);

  // ----------- Alta -----------
  protected readonly createForm: FormGroup = this.fb.group({
    name: [
      '',
      NAME_VALIDATORS,
      [uniqueValue((name) => this.methodService.existsByName(name), DUPLICATED_ENTITY)],
    ],
  });

  // ----------- Edicion -----------
  protected readonly editingId = signal<number | null>(null);

  private readonly editingMethod = computed(() =>
    this.methods.hasValue()
      ? this.methods.value().find((method) => method.id === this.editingId())
      : undefined,
  );

  protected readonly editForm: FormGroup = this.fb.group({
    name: [
      '',
      NAME_VALIDATORS,
      [
        uniqueValue((name) => {
          const method = this.editingMethod();
          return method ? this.methodService.existsByName(name, method.id) : of(false);
        }, DUPLICATED_ENTITY),
      ],
    ],
  });

  protected onCreate() {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    // Esperamos a que termine la validacion asincrona
    if (this.createForm.pending || this.saving()) return;

    const { name } = this.formUtils.trimValues(this.createForm.value);

    this.saving.set(true);
    this.methodService
      .create({ name })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success(`Método de pago "${name}" agregado`);
          this.createForm.reset({ name: '' });
          this.methods.reload();
        },
        error: () => {}, // El aviso lo muestra el interceptor
      });
  }

  protected startEdit(method: CustomPaymentMethod) {
    this.editingId.set(method.id);
    this.editForm.reset({ name: method.name });
  }

  protected cancelEdit() {
    this.editingId.set(null);
  }

  protected saveEdit(method: CustomPaymentMethod) {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    if (this.editForm.pending || this.saving()) return;

    const { name } = this.formUtils.trimValues(this.editForm.value);

    // Sin cambios: no hace falta llamar al backend
    if (name === method.name) {
      this.cancelEdit();
      return;
    }

    this.saving.set(true);
    this.methodService
      .update(method.id, name)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Método de pago actualizado');
          this.cancelEdit();
          this.methods.reload();
        },
        error: () => {},
      });
  }

  protected remove(method: CustomPaymentMethod) {
    this.runAction(this.methodService.deleteById(method.id), `"${method.name}" eliminado`);
  }

  protected restore(method: CustomPaymentMethod) {
    this.runAction(this.methodService.activate(method.id), `"${method.name}" reactivado`);
  }

  private runAction(request: Observable<unknown>, message: string) {
    if (this.saving()) return;

    this.saving.set(true);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notifications.success(message);
        this.methods.reload();
      },
      error: () => {},
    });
  }
}
