import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import translatePurchaseType, { purchaseTypeOptionsFor } from '../../utils/purchase.utils';
import { PurchaseService } from '../../services/purchase.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { PurchaseRequest } from '../../interfaces/purchase.interface';
import { SupplierType } from '../../../../supplier/interfaces/supplier.interface';
import { AppValidators } from '../../../../shared/validators/app-validators';

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
    unit: [1, [Validators.required, AppValidators.integer(1, 9_999)]],
    kilograms: ['', [Validators.required, AppValidators.amount(99_999.99)]],
    pricePerKilo: ['', [Validators.required, AppValidators.amount(9_999_999.99)]],
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

    // Armamos el request explicitamente (los números llegan como texto: se convierten acá)
    const { purchaseType, unit, kilograms, pricePerKilo } = this.purchaseForm.value;
    const request: PurchaseRequest = {
      purchaseType,
      unit: this.formUtils.parseDecimal(unit)!,
      kilograms: this.formUtils.parseDecimal(kilograms)!,
      pricePerKilo: this.formUtils.parseDecimal(pricePerKilo)!,
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
