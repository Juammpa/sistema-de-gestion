import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../utils/form-utils';
import { PaymentService } from '../../services/payment.service';
import { PAYMENT_TYPE_OPTIONS } from '../../utils/payment.utils';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';
import translatePaymentType from '../../utils/payment.utils';
import { Payment, PaymentRequest } from '../../interfaces/payment.interface';

@Component({
  selector: 'edit-payment-modal',
  imports: [ReactiveFormsModule, DeleteModal],
  templateUrl: './edit-payment-modal.html',
})
export class EditPaymentModal {
  private fb = inject(FormBuilder);
  private paymentService = inject(PaymentService);
  formUtils = FormUtils;

  paymentTypeOptions = PAYMENT_TYPE_OPTIONS;
  translatePaymentType = translatePaymentType;

  // Inputs
  payment = input.required<Payment>();

  // Definimos el formulario
  paymentForm: FormGroup = this.fb.group({
    paymentType: [null as string | null, [Validators.required]],
    amount: [0, [Validators.required, Validators.min(0)]],
  });

  // Output
  cancel = output();
  saved = output();
  deleted = output();

  close() {
    this.cancel.emit();
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
      error: (err) => console.error('Error al guardar el pago', err),
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.paymentService.deletePaymentById(this.payment().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: (err) => {
        console.error('Error al eliminar el pago', err);
        this.deleting.set(false);
      },
    });
  }
}
