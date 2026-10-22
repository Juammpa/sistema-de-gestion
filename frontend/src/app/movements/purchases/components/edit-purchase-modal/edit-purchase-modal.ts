import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PurchaseService } from '../../services/purchase.service';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { Purchase, PurchaseRequest } from '../../interfaces/purchase.interface';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';
import { SupplierType } from '../../../../supplier/interfaces/supplier.interface';
import { toIsoDate } from '../../../../shared/utils/date.utils';
import { AppValidators } from '../../../../shared/validators/app-validators';
import { PurchaseTypeService } from '../../../../purchase-type/services/purchase-type.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { purchaseTypesFor } from '../../../../purchase-type/utils/purchase-type.utils';

@Component({
  selector: 'edit-purchase-modal',
  imports: [ReactiveFormsModule, DeleteModal],
  templateUrl: './edit-purchase-modal.html',
})
export class EditPurchaseModal implements OnInit {
  private purchaseTypeService = inject(PurchaseTypeService);
  private purchaseService = inject(PurchaseService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  purchase = input.required<Purchase>();
  supplierType = input<SupplierType>('BOTH');

  purchaseTypes = rxResource({
    stream: () => this.purchaseTypeService.getAll(),
  });

  // Tipos activos que admite el proveedor.
  // Si la compra tiene un tipo ya eliminado, se conserva en la lista para poder editarla sin cambiarlo.
  purchaseTypeOptions = computed(() => {
    if (!this.purchaseTypes.hasValue()) return [];

    const types = this.purchaseTypes.value();
    const options = purchaseTypesFor(types, this.supplierType());
    const current = types.find((type) => type.id === this.purchase().purchaseTypeId);

    return current && !options.some((option) => option.id === current.id)
      ? [current, ...options]
      : options;
  });

  // Tope del selector de fecha: no se puede elegir un dia futuro.
  today = toIsoDate(new Date());

  purchaseForm: FormGroup = this.fb.group({
    date: [null as string | null, [Validators.required, AppValidators.notFutureDate]],
    purchaseTypeId: [null as number | null, [Validators.required]],
    unit: [1, [Validators.required, AppValidators.integer(1, 9_999)]],
    kilograms: ['', [Validators.required, AppValidators.amount(99_999.99)]],
    pricePerKilo: ['', [Validators.required, AppValidators.amount(9_999_999.99)]],
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
      purchaseTypeId: current.purchaseTypeId,
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

    const { date, purchaseTypeId, unit, kilograms, pricePerKilo } = this.purchaseForm.value;
    const request: PurchaseRequest = {
      date,
      purchaseTypeId,
      unit: this.formUtils.parseDecimal(unit)!,
      kilograms: this.formUtils.parseDecimal(kilograms)!,
      pricePerKilo: this.formUtils.parseDecimal(pricePerKilo)!,
      currentAccountId: this.purchase().currentAccountId,
    };

    this.purchaseService.updatePurchase(this.purchase().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.purchaseService.deletePurchaseById(this.purchase().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: (err) => this.deleting.set(false), // El aviso lo muestra el interceptor
    });
  }
}
