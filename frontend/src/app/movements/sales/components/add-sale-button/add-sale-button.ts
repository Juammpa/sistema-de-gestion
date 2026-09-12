import { Component, output } from '@angular/core';

@Component({
  selector: 'add-sale-button',
  imports: [],
  templateUrl: './add-sale-button.html',
})
export class AddSaleButton {
  // Output
  addSale = output();
}
