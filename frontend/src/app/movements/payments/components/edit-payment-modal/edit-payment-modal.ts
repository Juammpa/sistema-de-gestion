import { Component, computed, effect, inject, input, OnInit, output, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidatorFn,
  Validators,
} from '@angular/forms';
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
import { toIsoDate } from '../../../../shared/utils/date.utils';
import { AppValidators } from '../../../../shared/validators/app-validators';

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

  // Tope del selector de fecha: no se puede elegir un dia futuro.
  today = toIsoDate(new Date());

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

  // Un pago en efectivo a un proveedor no puede superar el efectivo disponible en caja
  private withinCashBalance: ValidatorFn = (control) => {
    const isCashWithdrawal =
      this.showCashBalance() && this.paymentForm?.controls['paymentType'].value === 'CASH';
    if (!isCashWithdrawal || !this.cashBalance.hasValue()) return null;

    const balance = this.cashBalance.value();
    const amount = this.formUtils.parseDecimal(control.value);
    if (balance == null || amount === null) return null;

    // El pago que se edita ya está descontado de la caja: se suma de nuevo
    const previous = this.payment().paymentType === 'CASH' ? this.payment().amount : 0;
    const available = balance + previous;

    return amount > available ? { insufficientCash: { available } } : null;
  };

  // Definimos el formulario
  paymentForm: FormGroup = this.fb.group({
    date: [null as string | null, [Validators.required, AppValidators.notFutureDate]],
    paymentType: [null as string | null, [Validators.required]],
    amount: [
      '',
      [Validators.required, AppValidators.amount(999_999_999.99), this.withinCashBalance],
    ],
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

      // El tope de caja solo aplica al efectivo: al cambiar el tipo hay que revalidar el monto
      this.paymentForm.controls['amount'].updateValueAndValidity();
    });

    // Cuando termina de cargar el saldo de caja, también se revalida el monto
    effect(() => {
      this.cashBalance.hasValue();
      this.paymentForm.controls['amount'].updateValueAndValidity();
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

    const { date, paymentType, amount, supplierId } = this.paymentForm.value;
    const request: PaymentRequest = {
      date,
      paymentType,
      amount: this.formUtils.parseDecimal(amount)!,
      supplierId,
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
