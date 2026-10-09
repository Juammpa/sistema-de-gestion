import { Component, inject, output } from '@angular/core';
import { CutMeatService } from '../../services/cut-meat.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../shared/utils/form-utils';
import { CutMeatRequest } from '../../interfaces/cut-meat.interface';
import { SPECIE_OPTIONS } from '../../utils/specie.utils';
import { AppValidators } from '../../../shared/validators/app-validators';
import { uniqueValue } from '../../../shared/validators/async-validators';
import { of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'cut-meat-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './cut-meat-form-modal.html',
})
export class CutMeatFormModal {
  private cutMeatService = inject(CutMeatService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Outputs
  created = output();
  cancel = output();

  specieOptions = SPECIE_OPTIONS;

  // Definimos el formulario
  cutMeatForm: FormGroup = this.fb.group({
    cutMeatName: [
      '',
      [Validators.required, AppValidators.noBlank, AppValidators.trimmedLength(2, 60)],
      [
        uniqueValue((name, control) => {
          const specie = control.parent?.get('specie')?.value;
          return specie ? this.cutMeatService.existsByNameAndSpecie(name, specie) : of(false);
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

    // Textos sin espacios sobrantes y precio como número (acepta coma o punto)
    const { cutMeatName, pricePerKilo, specie } = this.formUtils.trimValues(this.cutMeatForm.value);
    const request: CutMeatRequest = {
      cutMeatName,
      pricePerKilo: this.formUtils.parseDecimal(pricePerKilo)!,
      specie,
    };

    this.cutMeatService.createCutMeat(request).subscribe({
      next: () => {
        this.created.emit();
        this.cutMeatForm.reset();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }
}
