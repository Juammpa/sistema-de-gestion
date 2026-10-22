import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AppValidators } from '../../../shared/validators/app-validators';
import { SPECIE_OPTIONS, SpecieValue } from '../../../cut-meat/utils/specie.utils';
import { ErrorState } from '../../../shared/components/error-state/error-state';
import { PurchaseTypeService } from '../../../purchase-type/services/purchase-type.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { SETTINGS_UI } from '../../utils/settings-ui';
import { FormUtils } from '../../../shared/utils/form-utils';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { uniqueValue } from '../../../shared/validators/async-validators';
import { finalize, of, map, Observable } from 'rxjs';
import { PurchaseType } from '../../../purchase-type/interfaces/purchase-type.interface';

const NAME_VALIDATORS = [
  Validators.required,
  AppValidators.noBlank,
  AppValidators.trimmedLength(2, 60),
];

const GROUP_TITLES: Record<SpecieValue, string> = {
  BOVINE: 'Bovinos',
  PORCINE: 'Porcinos',
};

const ICON_BUTTON =
  'flex size-10 shrink-0 cursor-pointer items-center justify-center rounded-lg text-slate-400 transition-colors duration-150 hover:bg-slate-800/70 hover:text-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-400/70 disabled:cursor-not-allowed disabled:opacity-50';

@Component({
  selector: 'purchase-types-settings',
  imports: [ReactiveFormsModule, ErrorState],
  templateUrl: './purchase-types-settings.html',
})
export class PurchaseTypesSettings {
  private purchaseTypeService = inject(PurchaseTypeService);
  private notifications = inject(NotificationService);
  private fb = inject(FormBuilder);

  protected readonly ui = SETTINGS_UI;
  protected readonly formUtils = FormUtils;
  protected readonly specieOptions = SPECIE_OPTIONS;
  protected readonly iconButton = ICON_BUTTON;
  protected readonly dangerIconButton = `${ICON_BUTTON} hover:!text-red-300`;

  protected readonly types = rxResource({
    stream: () => this.purchaseTypeService.getAll(),
  });

  // Activos y eliminados, agrupados por especie
  protected readonly groups = computed(() => {
    const all = this.types.hasValue() ? this.types.value() : [];

    return SPECIE_OPTIONS.map((option) => {
      const ofSpecie = all.filter((type) => type.specie === option.value);

      return {
        specie: option.value,
        title: GROUP_TITLES[option.value],
        active: ofSpecie.filter((type) => type.isActive),
        archived: ofSpecie.filter((type) => !type.isActive),
      };
    });
  });

  // Evita acciones superpuestas (guardar, eliminar, reactivar)
  protected readonly saving = signal(false);

  // ----------- Alta -----------
  protected readonly createForm: FormGroup = this.fb.group({
    name: [
      '',
      NAME_VALIDATORS,
      [
        uniqueValue((name, control) => {
          const specie = control.parent?.get('specie')?.value;
          return specie ? this.purchaseTypeService.existsByNameAndSpecie(name, specie) : of(false);
        }, 'un tipo de compra de esa especie'),
      ],
    ],
    specie: [null as SpecieValue | null, [Validators.required]],
  });

  // ----------- Edicion -----------
  protected readonly editingId = signal<number | null>(null);

  private readonly editingType = computed(() =>
    this.types.hasValue()
      ? this.types.value().find((type) => type.id === this.editingId())
      : undefined,
  );

  protected readonly editForm: FormGroup = this.fb.group({
    name: [
      '',
      NAME_VALIDATORS,
      [
        uniqueValue((name) => {
          const type = this.editingType();
          return type
            ? this.purchaseTypeService.existsByNameAndSpecie(name, type.specie, type.id)
            : of(false);
        }, 'un tipo de compra de esa especie'),
      ],
    ],
  });

  constructor() {
    // La unicidad depende de la especie: si cambia, hay que volver a validar el nombre
    this.createForm.controls['specie'].valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.createForm.controls['name'].updateValueAndValidity());
  }

  protected onCreate() {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    // Esperamos a que termine la validacion asincrona
    if (this.createForm.pending || this.saving()) return;

    const { name, specie } = this.formUtils.trimValues(this.createForm.value);

    this.saving.set(true);
    this.purchaseTypeService
      .create({ name, specie })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success(`Tipo de compra "${name} agregado"`);
          // Se conserva la especie para cargar varios seguidos
          this.createForm.reset({ name: '', specie });
          this.types.reload();
        },
        error: () => {}, // El aviso lo muestra el interceptor
      });
  }

  protected startEdit(type: PurchaseType) {
    this.editingId.set(type.id);
    this.editForm.reset({ name: type.name });
  }

  protected cancelEdit() {
    this.editingId.set(null);
  }

  protected saveEdit(type: PurchaseType) {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    if (this.editForm.pending || this.saving()) return;

    const { name } = this.formUtils.trimValues(this.editForm.value);

    // Sin cambios: no hace falta llamar al backend
    if (name === type.name) {
      this.cancelEdit();
      return;
    }

    this.saving.set(true);
    this.purchaseTypeService
      .update(type.id, { name, specie: type.specie })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Tipo de compra actualizado');
          this.cancelEdit();
          this.types.reload();
        },
        error: () => {},
      });
  }

  protected remove(type: PurchaseType) {
    this.runAction(this.purchaseTypeService.deleteById(type.id), `"${type.name}" eliminado`);
  }

  protected restore(type: PurchaseType) {
    this.runAction(this.purchaseTypeService.activate(type.id), `"${type.name}" reactivado`);
  }

  private runAction(request: Observable<unknown>, message: string) {
    if (this.saving()) return;

    this.saving.set(true);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notifications.success(message);
        this.types.reload();
      },
      error: () => {},
    });
  }
}
