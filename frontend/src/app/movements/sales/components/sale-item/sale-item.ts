import { Component, input } from '@angular/core';
import { SaleMovement } from '../../../interfaces/movement.interface';
import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';

@Component({
  selector: '[sale-item]',
  imports: [CurrencyPipe, DatePipe, TitleCasePipe],
  templateUrl: './sale-item.html',
})
export class SaleItem {
  // Inputs
  sale = input.required<SaleMovement>();
}
