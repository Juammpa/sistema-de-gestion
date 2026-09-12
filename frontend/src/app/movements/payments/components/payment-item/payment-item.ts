import { Component, input } from '@angular/core';
import { PaymentMovement } from '../../../interfaces/movement.interface';

@Component({
  selector: '[payment-item]',
  imports: [],
  templateUrl: './payment-item.html',
})
export class PaymentItem {
  payment = input.required<PaymentMovement>();
}
