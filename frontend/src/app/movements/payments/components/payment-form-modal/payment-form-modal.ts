import { Component, computed, effect, inject, input, output } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { PaymentService } from '../../services/payment.service';
import { PaymentRequest } from '../../interfaces/payment.interface';
import { PAYMENT_TYPE_OPTIONS } from '../../utils/payment.utils';
import { SupplierService } from '../../../../supplier/services/supplier.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { CashService } from '../../../../cash/services/cash.service';
import { CurrencyPipe } from '@angular/common';
import { AppValidators } from '../../../../shared/validators/app-validators';

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

  // Un pago a un proveedor no puede superar lo disponible en caja, sea cual sea el metodo
  private withinCashBalance: ValidatorFn = (control) => {
    if (!this.showCashBalance() || !this.cashBalance.hasValue()) return null;

    const available = this.cashBalance.value();
    const amount = this.formUtils.parseDecimal(control.value);
    if (available == null || amount === null) return null;

    return amount > available ? { insufficientCash: { available } } : null;
  };

  // Definimos nuestro formulario
  paymentForm: FormGroup = this.fb.group({
    paymentType: [null as string | null, [Validators.required]],
    amount: [
      '',
      [Validators.required, AppValidators.amount(999_999_999.99), this.withinCashBalance],
    ],
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

    // Cuando termina de cargar el saldo de caja, tambien se revalida el monto
    effect(() => {
      this.cashBalance.hasValue();
      this.paymentForm.controls['amount'].updateValueAndValidity();
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

    // Armamos el request explicitamente (el monto llega como texto: se convierte acá)
    const { paymentType, amount, supplierId } = this.paymentForm.value;
    const request: PaymentRequest = {
      paymentType,
      amount: this.formUtils.parseDecimal(amount)!,
      supplierId,
      currentAccountId: this.currentAccountId(),
    };

    // Creamos el pago
    this.paymentService.createPayment(request).subscribe({
      next: () => {
        this.close();
        this.saved.emit();
        this.paymentForm.reset();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }
}
