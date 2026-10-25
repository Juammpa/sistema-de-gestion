import { Component, computed, input } from '@angular/core';
import { PaymentMovement } from '../../../interfaces/movement.interface';
import { CurrencyPipe, DatePipe } from '@angular/common';
import translatePaymentType from '../../utils/payment.utils';

@Component({
  selector: '[payment-item]',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './payment-item.html',
})
export class PaymentItem {
  translatePaymentType = translatePaymentType;
  payment = input.required<PaymentMovement>();

  isRejection = computed(() => this.payment().paymentType === 'CHECK_REJECTION');
}
