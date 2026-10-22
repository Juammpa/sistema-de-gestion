import { Component, computed, effect, inject, input, output } from '@angular/core';
import { PurchaseService } from '../../services/purchase.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { PurchaseRequest } from '../../interfaces/purchase.interface';
import { SupplierType } from '../../../../supplier/interfaces/supplier.interface';
import { AppValidators } from '../../../../shared/validators/app-validators';
import { PurchaseTypeService } from '../../../../purchase-type/services/purchase-type.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { purchaseTypesFor } from '../../../../purchase-type/utils/purchase-type.utils';

@Component({
  selector: 'purchase-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './purchase-form-modal.html',
})
export class PurchaseFormModal {
  private purchaseService = inject(PurchaseService);
  private purchaseTypeService = inject(PurchaseTypeService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  currentAccountId = input.required<number>();
  supplierType = input<SupplierType>('BOTH');

  // Outputs
  cancel = output();
  saved = output();

  // Tipos de compra del usuario
  purchaseTypes = rxResource({
    stream: () => this.purchaseTypeService.getAll(),
  });

  // Lo que puede vender este proveedor
  purchaseTypeOptions = computed(() =>
    this.purchaseTypes.hasValue()
      ? purchaseTypesFor(this.purchaseTypes.value(), this.supplierType())
      : [],
  );

  // Definimos nuestros formulario
  purchaseForm: FormGroup = this.fb.group({
    purchaseTypeId: [null as number | null, [Validators.required]],
    unit: [1, [Validators.required, AppValidators.integer(1, 9_999)]],
    kilograms: ['', [Validators.required, AppValidators.amount(99_999.99)]],
    pricePerKilo: ['', [Validators.required, AppValidators.amount(9_999_999.99)]],
  });

  constructor() {
    // Si el proveedor solo admite un tipo, queda elegido
    effect(() => {
      const options = this.purchaseTypeOptions();
      if (options.length === 1)
        this.purchaseForm.patchValue({
          purchaseTypeId: options[0].id,
        });
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
    const { purchaseTypeId, unit, kilograms, pricePerKilo } = this.purchaseForm.value;
    const request: PurchaseRequest = {
      purchaseTypeId,
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
