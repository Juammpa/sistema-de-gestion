import { Component, output } from '@angular/core';

@Component({
  selector: 'add-purchase-button',
  imports: [],
  templateUrl: './add-purchase-button.html',
})
export class AddPurchaseButton {
  // Output
  addPurchase = output();
}
