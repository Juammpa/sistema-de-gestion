import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { PaymentService } from '../../services/payment.service';
import { PAYMENT_TYPE_OPTIONS } from '../../utils/payment.utils';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';
import translatePaymentType from '../../utils/payment.utils';
import { Payment, PaymentRequest } from '../../interfaces/payment.interface';
import { SupplierService } from '../../../../supplier/services/supplier.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { CashService } from '../../../../cash/services/cash.service';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'edit-payment-modal',
  imports: [ReactiveFormsModule, DeleteModal, CurrencyPipe],
  templateUrl: './edit-payment-modal.html',
})
export class EditPaymentModal implements OnInit {
  private fb = inject(FormBuilder);
  private paymentService = inject(PaymentService);
  private supplierService = inject(SupplierService);
  private cashService = inject(CashService);
  formUtils = FormUtils;

  translatePaymentType = translatePaymentType;

  // Inputs
  payment = input.required<Payment>();
  allowSupplierTransfer = input<boolean>(false);
  showCashBalance = input<boolean>(false); // true solo en la pagina de proveedor

  paymentTypeOptions = computed(() =>
    PAYMENT_TYPE_OPTIONS.filter(
      (option) => option.value !== 'SUPPLIER_TRANSFER' || this.allowSupplierTransfer(),
    ),
  );

  cashBalance = rxResource({
    params: () => this.showCashBalance(),
    stream: ({ params }) => (params ? this.cashService.getBalance() : of(null)),
  });

  suppliers = rxResource({
    params: () => this.allowSupplierTransfer(),
    stream: ({ params }) => (params ? this.supplierService.getAllSuppliers() : of([])),
  });

  // Definimos el formulario
  paymentForm: FormGroup = this.fb.group({
    date: [null as string | null, [Validators.required]],
    paymentType: [null as string | null, [Validators.required]],
    amount: [0, [Validators.required, Validators.min(0)]],
    supplierId: [null as number | null],
  });

  // Output
  cancel = output();
  saved = output();
  deleted = output();

  constructor() {
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

  close() {
    this.cancel.emit();
  }

  ngOnInit() {
    const current = this.payment();
    this.paymentForm.patchValue({
      date: current.date,
      paymentType: current.paymentType,
      supplierId: current.supplierId,
      amount: current.amount,
    });

    // Un pago espejo (generado automaticamente) es de solo lectura
    if (current.mirror) {
      this.paymentForm.disable();
    }
  }

  // Señales
  showDeleteConfirm = signal<boolean>(false);
  deleting = signal<boolean>(false);

  onSave() {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    // Armamos el request explicitamente
    const request: PaymentRequest = {
      ...this.paymentForm.value,
      currentAccountId: this.payment().currentAccountId,
    };

    this.paymentService.updatePayment(this.payment().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.paymentService.deletePaymentById(this.payment().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: () => this.deleting.set(false), // El aviso lo muestra el interceptor
    });
  }
}
