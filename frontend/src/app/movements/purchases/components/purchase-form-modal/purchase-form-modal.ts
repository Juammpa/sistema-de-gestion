import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import translatePurchaseType, { purchaseTypeOptionsFor } from '../../utils/purchase.utils';
import { PurchaseService } from '../../services/purchase.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { PurchaseRequest } from '../../interfaces/purchase.interface';
import { SupplierType } from '../../../../supplier/interfaces/supplier.interface';

@Component({
  selector: 'purchase-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './purchase-form-modal.html',
})
export class PurchaseFormModal implements OnInit {
  private purchaseService = inject(PurchaseService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;
  translatePurchaseType = translatePurchaseType;

  // Inputs
  currentAccountId = input.required<number>();
  supplierType = input<SupplierType>('BOTH');

  // Outputs
  cancel = output();
  saved = output();

  // Arreglo de Tipos de Compra
  purchaseTypesOptions = computed(() => purchaseTypeOptionsFor(this.supplierType()));

  // Definimos nuestros formulario
  purchaseForm: FormGroup = this.fb.group({
    purchaseType: [null as string | null, [Validators.required]],
    unit: [1, [Validators.required, Validators.min(1)]],
    kilograms: [1, [Validators.required, Validators.min(1)]],
    pricePerKilo: [1, [Validators.required, Validators.min(1)]],
  });

  ngOnInit() {
    const options = this.purchaseTypesOptions();
    if (options.length === 1)
      this.purchaseForm.patchValue({
        purchaseType: options[0].value,
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

    /// Armamos el request explicitamente
    const request: PurchaseRequest = {
      ...this.purchaseForm.value,
      currentAccountId: this.currentAccountId(),
    };

    // Creamos la compra
    this.purchaseService.createPurchase(request).subscribe({
      next: () => {
        this.close();
        this.saved.emit();
        this.purchaseForm.reset();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }
}
