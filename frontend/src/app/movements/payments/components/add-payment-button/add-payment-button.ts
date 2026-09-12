import { Component, output } from '@angular/core';

@Component({
  selector: 'add-payment-button',
  imports: [],
  templateUrl: './add-payment-button.html',
})
export class AddPaymentButton {
  // Output
  addPayment = output();
}
