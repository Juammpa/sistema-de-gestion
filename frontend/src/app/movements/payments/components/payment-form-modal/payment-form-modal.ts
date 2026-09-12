import { Component, inject, input, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../utils/form-utils';
import { Client } from '../../../../clientes/interfaces/client.interface';
import { PaymentService } from '../../services/payment.service';
import { PaymentRequest } from '../../interfaces/payment.interface';
import { ButcherShop } from '../../../../butcher-shop/interfaces/butcher-shop.interface';
import { PAYMENT_TYPE_OPTIONS } from '../../utils/payment.utils';

@Component({
  selector: 'payment-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './payment-form-modal.html',
})
export class PaymentFormModal {
  private paymentService = inject(PaymentService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Input
  client = input.required<Client>();
  butcherShop = input.required<ButcherShop>();

  // Output
  cancel = output();
  saved = output();

  // Arreglo de Tipos de metodos de pago
  paymentTypesOptions = PAYMENT_TYPE_OPTIONS;

  // Definimos nuestro formulario
  paymentForm: FormGroup = this.fb.group({
    paymentType: [null as string | null, [Validators.required]],
    amount: [0, [Validators.required, Validators.min(0)]],
  });

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
      clientId: this.client().id,
      butcherShopId: this.butcherShop().id,
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
