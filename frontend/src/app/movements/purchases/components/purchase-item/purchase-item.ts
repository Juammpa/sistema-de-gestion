import { Component, input } from '@angular/core';
import translatePurchaseType from '../../utils/purchase.utils';
import { PurchaseMovement } from '../../../interfaces/movement.interface';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: '[purchase-item]',
  imports: [CurrencyPipe],
  templateUrl: './purchase-item.html',
})
export class PurchaseItem {
  translatePurchaseType = translatePurchaseType;

  // Inputs
  purchase = input.required<PurchaseMovement>();
}
