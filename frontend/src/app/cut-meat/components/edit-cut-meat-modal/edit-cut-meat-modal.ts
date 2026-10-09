import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { CutMeatService } from '../../services/cut-meat.service';
import { CutMeat, CutMeatRequest } from '../../interfaces/cut-meat.interface';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../shared/utils/form-utils';
import { DeleteModal } from '../../../shared/components/delete-modal/delete-modal';
import { SPECIE_OPTIONS } from '../../utils/specie.utils';
import { AppValidators } from '../../../shared/validators/app-validators';
import { uniqueValue } from '../../../shared/validators/async-validators';
import { of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'edit-cut-meat-modal',
  imports: [ReactiveFormsModule, DeleteModal],
  templateUrl: './edit-cut-meat-modal.html',
})
export class EditCutMeatModal implements OnInit {
  private cutMeatService = inject(CutMeatService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  cutMeat = input.required<CutMeat>();

  // Outputs
  cancel = output();
  deleted = output();
  saved = output();

  specieOptions = SPECIE_OPTIONS;

  // Señales
  showDeleteConfirm = signal<boolean>(false);
  deleting = signal<boolean>(false);
  activating = signal<boolean>(false);

  cutMeatForm: FormGroup = this.fb.group({
    cutMeatName: [
      '',
      [Validators.required, AppValidators.noBlank, AppValidators.trimmedLength(2, 60)],
      [
        uniqueValue((name, control) => {
          const specie = control.parent?.get('specie')?.value;
          return specie
            ? this.cutMeatService.existsByNameAndSpecie(name, specie, this.cutMeat().id)
            : of(false);
        }, 'un corte de esa especie'),
      ],
    ],
    pricePerKilo: ['', [Validators.required, AppValidators.amount(9_999_999.99)]],
    specie: [null as string | null, [Validators.required]],
  });

  constructor() {
    // La unicidad depende de la especie: si cambia, hay que volver a validar el nombre
    this.cutMeatForm.controls['specie'].valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.cutMeatForm.controls['cutMeatName'].updateValueAndValidity());
  }

  ngOnInit() {
    const current = this.cutMeat();
    this.cutMeatForm.patchValue({
      cutMeatName: current.cutMeatName,
      pricePerKilo: current.pricePerKilo,
      specie: current.specie,
    });
  }

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.cutMeatForm.invalid) {
      this.cutMeatForm.markAllAsTouched();
      return;
    }

    // Esperamos a que termine la validación asíncrona
    if (this.cutMeatForm.pending) return;

    const { cutMeatName, pricePerKilo, specie } = this.formUtils.trimValues(this.cutMeatForm.value);
    const request: CutMeatRequest = {
      cutMeatName,
      pricePerKilo: this.formUtils.parseDecimal(pricePerKilo)!,
      specie,
    };

    this.cutMeatService.updateCutMeat(this.cutMeat().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.cutMeatService.deleteCutMeatById(this.cutMeat().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: () => this.deleting.set(false), // El aviso lo muestra el interceptor
    });
  }

  onActivate() {
    this.activating.set(true);

    this.cutMeatService.activateCutMeatById(this.cutMeat().id).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: () => this.activating.set(false), // El aviso lo muestra el interceptor
    });
  }
}
