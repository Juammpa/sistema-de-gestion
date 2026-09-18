import { Component, inject, output } from '@angular/core';
import { CutMeatService } from '../../services/cut-meat.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../utils/form-utils';
import { CutMeatRequest } from '../../interfaces/cut-meat.interface';

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

  // Definimos el formulario
  cutMeatForm: FormGroup = this.fb.group({
    cutMeatName: ['', [Validators.required, Validators.minLength(3)]],
    pricePerKilo: [0, [Validators.required, Validators.min(1)]],
  });

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.cutMeatForm.invalid) {
      this.cutMeatForm.markAllAsTouched();
      return;
    }

    const request: CutMeatRequest = this.cutMeatForm.value;

    this.cutMeatService.createCutMeat(request).subscribe({
      next: () => {
        this.created.emit();
        this.cutMeatForm.reset();
        this.close();
      },
      error: (err) => console.error('Error al crear corte de carne', err),
    });
  }
}
