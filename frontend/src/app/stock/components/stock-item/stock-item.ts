import { Component, computed, input, output } from '@angular/core';
import { StockItem } from '../../interfaces/stock.interface';
import { SUPPLIER_TYPE_INFO } from '../../../supplier/utils/supplier-type.utils';
import { formatIsoDate } from '../../../shared/utils/date.utils';
import { DecimalPipe } from '@angular/common';

const BASE_CLASS =
  'flex items-center gap-3 rounded-2xl border border-line bg-panel-from px-4 py-3 shadow-sm shadow-black/20 ';

// Solo las piezas editables se ven clickables
const EDITABLE_CLASS =
  'cursor-pointer transition-colors duration-150 hover:border-accent-500/50 hover:bg-accent-500/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-400/70 motion-reduce:transition-none';

@Component({
  selector: 'stock-item',
  imports: [DecimalPipe],
  templateUrl: './stock-item.html',
  host: { class: 'block' },
})
export class StockItemCard {
  item = input.required<StockItem>();

  // Pide ajustar los pesos de la compra (solo piezas editables)
  edit = output<StockItem>();

  protected specie = computed(() => SUPPLIER_TYPE_INFO[this.item().specie]);
  protected typeLabel = computed(() => this.item().purchaseTypeName);
  protected date = computed(() => formatIsoDate(this.item().date));
  protected cardClass = computed(() => BASE_CLASS + (this.item().editable ? EDITABLE_CLASS : ''));

  protected onActivate() {
    if (this.item().editable) this.edit.emit(this.item());
  }
}
