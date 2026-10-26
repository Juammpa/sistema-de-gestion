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
import { PAYMENT_TYPE_OPTIONS, PaymentTypeValue } from '../../utils/payment.utils';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';
import translatePaymentType from '../../utils/payment.utils';
import { Payment, PaymentRequest } from '../../interfaces/payment.interface';
import { SupplierService } from '../../../../supplier/services/supplier.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { CashService } from '../../../../cash/services/cash.service';
import { CurrencyPipe } from '@angular/common';
import { DateField } from '../../../../shared/components/date-field/date-field';
import { toIsoDate } from '../../../../shared/utils/date.utils';
import { AppValidators } from '../../../../shared/validators/app-validators';

@Component({
  selector: 'edit-payment-modal',
  imports: [ReactiveFormsModule, DeleteModal, DateField, CurrencyPipe],
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

  isRejection = computed(() => this.payment().paymentType === 'CHECK_REJECTION');

  // Inputs
  payment = input.required<Payment>();
  allowSupplierTransfer = input<boolean>(false);
  showCashBalance = input<boolean>(false); // true solo en la pagina de proveedor

  paymentTypeOptions = computed(() => {
    const options = PAYMENT_TYPE_OPTIONS.filter(
      (option) => option.value !== 'SUPPLIER_TRANSFER' || this.allowSupplierTransfer(),
    );

    // El rechazo no se elige a mano, pero se muestra al abrir uno
    return this.isRejection()
      ? [
          ...options,
          {
            value: 'CHECK_REJECTION' as PaymentTypeValue,
            label: translatePaymentType('CHECK_REJECTION'),
          },
        ]
      : options;
  });

  cashBalance = rxResource({
    params: () => this.showCashBalance(),
    stream: ({ params }) => (params ? this.cashService.getBalance() : of(null)),
  });

  suppliers = rxResource({
    params: () => this.allowSupplierTransfer(),
    stream: ({ params }) => (params ? this.supplierService.getAllSuppliers() : of([])),
  });

  // Un pago a un proveedor no puede superar lo disponible en caja, sea cual sea el metodo
  private withinCashBalance: ValidatorFn = (control) => {
    if (!this.showCashBalance() || !this.cashBalance.hasValue()) return null;

    const balance = this.cashBalance.value();
    const amount = this.formUtils.parseDecimal(control.value);
    if (balance == null || amount === null) return null;

    // El pago que se edita ya está descontado de la caja: se suma de nuevo
    const available = balance + this.payment().amount;

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

    // Solo lectura: pago espejo (generado automaticamente), cheque rechazado o su rechazo
    if (this.readOnly()) {
      this.paymentForm.disable();
    }
  }

  // Señales
  showDeleteConfirm = signal<boolean>(false);
  deleting = signal<boolean>(false);
  confirmingReject = signal<boolean>(false);
  rejecting = signal<boolean>(false);

  // Solo los cheques de clientes que todavia no fueron rechazados
  canReject = computed(
    () =>
      this.allowSupplierTransfer() &&
      this.payment().paymentType === 'CHECK' &&
      !this.payment().rejected,
  );

  // Solo lectura: pago espejo, cheque ya rechazado o el rechazo mismo
  readOnly = computed(() => this.payment().mirror || this.payment().rejected || this.isRejection());

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

  onReject() {
    this.rejecting.set(true);

    this.paymentService.rejectCheck(this.payment().id).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: () => this.rejecting.set(false), // El aviso lo muestra el interceptor
    });
  }
}
