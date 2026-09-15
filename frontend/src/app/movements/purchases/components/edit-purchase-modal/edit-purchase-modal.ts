import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PurchaseService } from '../../services/purchase.service';
import { FormUtils } from '../../../../utils/form-utils';
import { PURCHASE_TYPE_OPTIONS } from '../../utils/purchase.utils';
import translatePurchaseType from '../../utils/purchase.utils';
import { Purchase, PurchaseRequest } from '../../interfaces/purchase.interface';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';

@Component({
  selector: 'edit-purchase-modal',
  imports: [ReactiveFormsModule, DeleteModal],
  templateUrl: './edit-purchase-modal.html',
})
export class EditPurchaseModal implements OnInit {
  private fb = inject(FormBuilder);
  private purchaseService = inject(PurchaseService);
  formUtils = FormUtils;

  purchaseTypeOptions = PURCHASE_TYPE_OPTIONS;
  translatePurchaseType = translatePurchaseType;

  // Inputs
  purchase = input.required<Purchase>();

  purchaseForm: FormGroup = this.fb.group({
    date: [null as string | null, [Validators.required]],
    purchaseType: [null as string | null, [Validators.required]],
    unit: [1, [Validators.required, Validators.min(1)]],
    kilograms: [1, [Validators.required, Validators.min(1)]],
    pricePerKilo: [1, [Validators.required, Validators.min(1)]],
  });

  // Outputs
  cancel = output();
  saved = output();
  deleted = output();

  // Señales
  showDeleteConfirm = signal(false);
  deleting = signal(false);

  ngOnInit() {
    const current = this.purchase();
    this.purchaseForm.patchValue({
      date: current.date,
      purchaseType: current.purchaseType,
      unit: current.unit,
      kilograms: current.kilograms,
      pricePerKilo: current.pricePerKilo,
    });
  }

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.purchaseForm.invalid) {
      this.purchaseForm.markAllAsTouched();
      return;
    }

    const request: PurchaseRequest = {
      ...this.purchaseForm.value,
      currentAccountId: this.purchase().currentAccountId,
    };

    this.purchaseService.updatePurchase(this.purchase().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: (err) => console.error('Error al actualizar la compra', err),
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.purchaseService.deletePurchaseById(this.purchase().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: (err) => {
        console.error('Error al eliminar la compra', err);
        this.deleting.set(false);
      },
    });
  }
}
