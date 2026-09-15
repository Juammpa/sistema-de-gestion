import { Component, input } from '@angular/core';
import translatePurchaseType from '../../utils/purchase.utils';
import { PurchaseMovement } from '../../../interfaces/movement.interface';
import { CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: '[purchase-item]',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './purchase-item.html',
})
export class PurchaseItem {
  translatePurchaseType = translatePurchaseType;

  // Inputs
  purchase = input.required<PurchaseMovement>();
}
