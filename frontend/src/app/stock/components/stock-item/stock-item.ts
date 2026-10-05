import { Component, computed, input } from '@angular/core';
import { StockItem } from '../../interfaces/stock.interface';
import { SUPPLIER_TYPE_INFO } from '../../../supplier/utils/supplier-type.utils';
import translatePurchaseType from '../../../movements/purchases/utils/purchase.utils';
import { formatIsoDate } from '../../../shared/utils/date.utils';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'stock-item',
  imports: [DecimalPipe],
  templateUrl: './stock-item.html',
  host: { class: 'block' },
})
export class StockItemCard {
  item = input.required<StockItem>();

  protected specie = computed(() => SUPPLIER_TYPE_INFO[this.item().specie]);
  protected typeLabel = computed(() => translatePurchaseType(this.item().purchaseType));
  protected date = computed(() => formatIsoDate(this.item().date));
}
