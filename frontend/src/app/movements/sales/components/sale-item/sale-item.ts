import { Component, input } from '@angular/core';
import { translateCutMeatType } from '../../../../cut-meat/utils/cut-meat.utils';
import { SaleMovement } from '../../../interfaces/movement.interface';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: '[sale-item]',
  imports: [CurrencyPipe],
  templateUrl: './sale-item.html',
})
export class SaleItem {
  translateCutMeatType = translateCutMeatType;

  // Inputs
  sale = input.required<SaleMovement>();
}
