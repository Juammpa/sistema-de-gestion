import { Component, computed, inject, input, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { PaymentService } from '../../services/payment.service';
import { PaymentRequest } from '../../interfaces/payment.interface';
import { PAYMENT_TYPE_OPTIONS } from '../../utils/payment.utils';
import { SupplierService } from '../../../../supplier/services/supplier.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { CashService } from '../../../../cash/services/cash.service';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'payment-form-modal',
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './payment-form-modal.html',
})
export class PaymentFormModal {
  private paymentService = inject(PaymentService);
  private supplierService = inject(SupplierService);
  private cashService = inject(CashService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Input
  currentAccountId = input.required<number>();
  allowSupplierTransfer = input<boolean>(false);
  showCashBalance = input<boolean>(false); // true solo en la pagina de un proveedor

  // Output
  cancel = output();
  saved = output();

  // Tipos de pago (la transferencia a proveedor solo se ofrece en cuentas de clientes)
  paymentTypesOptions = computed(() =>
    PAYMENT_TYPE_OPTIONS.filter(
      (option) => option.value !== 'SUPPLIER_TRANSFER' || this.allowSupplierTransfer(),
    ),
  );

  cashBalance = rxResource({
    params: () => this.showCashBalance(),
    stream: ({ params }) => (params ? this.cashService.getBalance() : of(null)),
  });

  // Proveedores (solo se cargan si se permite la transferencia a proveedor)
  suppliers = rxResource({
    params: () => this.allowSupplierTransfer(),
    stream: ({ params }) => (params ? this.supplierService.getAllSuppliers() : of([])),
  });

  // Definimos nuestro formulario
  paymentForm: FormGroup = this.fb.group({
    paymentType: [null as string | null, [Validators.required]],
    amount: [0, [Validators.required, Validators.min(0)]],
    supplierId: [null as number | null],
  });

  constructor() {
    // El proveedor es obligatorio solo cuando el tipo es transferencia a proveedor
    this.paymentForm.controls['paymentType'].valueChanges.subscribe((type) => {
      const supplierControl = this.paymentForm.controls['supplierId'];

      if (type === 'SUPPLIER_TRANSFER') {
        supplierControl.setValidators([Validators.required]);
      } else {
        supplierControl.clearValidators();
        supplierControl.setValue(null);
      }
      supplierControl.updateValueAndValidity();
    });
  }

  get isSupplierTransfer() {
    return this.paymentForm.controls['paymentType'].value === 'SUPPLIER_TRANSFER';
  }

  // Cierra el modal
  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    // Armamos el request explicitamente
    const request: PaymentRequest = {
      ...this.paymentForm.value,
      currentAccountId: this.currentAccountId(),
    };

    // Creamos el pago
    this.paymentService.createPayment(request).subscribe({
      next: () => {
        this.close();
        this.saved.emit();
        this.paymentForm.reset();
      },
      error: (err) => console.error('Error al guardar el pago', err),
    });
  }
}
