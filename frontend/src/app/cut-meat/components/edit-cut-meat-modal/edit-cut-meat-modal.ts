import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { CutMeatService } from '../../services/cut-meat.service';
import { CutMeat, CutMeatRequest } from '../../interfaces/cut-meat.interface';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../utils/form-utils';
import { DeleteModal } from '../../../shared/components/delete-modal/delete-modal';

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

  // Señales
  showDeleteConfirm = signal<boolean>(false);
  deleting = signal<boolean>(false);

  CutMeatForm: FormGroup = this.fb.group({
    cutMeatName: ['', [Validators.required, Validators.minLength(3)]],
    pricePerKilo: [1, [Validators.required, Validators.min(1)]],
  });

  ngOnInit() {
    const current = this.cutMeat();
    this.CutMeatForm.patchValue({
      cutMeatName: current.cutMeatName,
      pricePerKilo: current.pricePerKilo,
    });
  }

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.CutMeatForm.invalid) {
      this.CutMeatForm.markAllAsTouched();
      return;
    }

    const request: CutMeatRequest = this.CutMeatForm.value;

    this.cutMeatService.updateCutMeat(this.cutMeat().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: (err) => console.error('Error al actualizar el corte de carne', err),
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.cutMeatService.deleteCutMeatById(this.cutMeat().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: (err) => {
        console.error('Error al eliminar el corte de carne', err);
        this.deleting.set(false);
      },
    });
  }
}
