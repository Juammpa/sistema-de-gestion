import { Component, computed, input, output } from '@angular/core';
import { StockItem } from '../../interfaces/stock.interface';
import { SUPPLIER_TYPE_INFO } from '../../../supplier/utils/supplier-type.utils';
import { formatIsoDate } from '../../../shared/utils/date.utils';
import { DecimalPipe, TitleCasePipe } from '@angular/common';

const CARD_CLASS =
  'rounded-2xl border border-line bg-panel-from px-4 py-3 shadow-sm shadow-black/20';

// Botones de accion de la tarjeta (pesos y cortes)
const ACTION_CLASS =
  'flex size-10 shrink-0 cursor-pointer items-center justify-center rounded-xl border border-transparent text-slate-400 transition-colors duration-150 hover:border-accent-500/40 hover:bg-accent-500/10 hover:text-accent-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-400/70 motion-reduce:transition-none';

const SOLD_OUT_CARD_CLASS =
  'rounded-2xl border border-rose-500/40 bg-rose-500/5 px-4 py-3 shadow-sm shadow-black/20';

const FOOTER_BUTTON_CLASS =
  'inline-flex min-h-9 shrink-0 cursor-pointer touch-manipulation items-center gap-1.5 rounded-lg border px-3 text-xs font-medium transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent-400/70 motion-reduce:transition-none';

const CLOSE_CLASS = `${FOOTER_BUTTON_CLASS} border-emerald-500/40 bg-emerald-500/10 text-emerald-300 hover:bg-emerald-500/20`;

const REOPEN_CLASS = `${FOOTER_BUTTON_CLASS} border-slate-700/70 bg-raised text-slate-300 hover:border-rose-500/50 hover:text-rose-300`;

@Component({
  selector: 'stock-item',
  imports: [DecimalPipe, TitleCasePipe],
  templateUrl: './stock-item.html',
  host: { class: 'block' },
})
export class StockItemCard {
  item = input.required<StockItem>();

  // Pide ajustar los pesos de la compra (solo piezas editables)
  edit = output<StockItem>();

  // Pide dividir la pieza en cortes
  editCuts = output<StockItem>();

  // Pide cerrar la pieza como vendida (solo si todos sus cortes estan vendidos)
  closePiece = output<StockItem>();

  // Pide reabrir una pieza cerrada
  reopenPiece = output<StockItem>();

  protected readonly cardClass = computed(() =>
    this.item().soldOut ? SOLD_OUT_CARD_CLASS : CARD_CLASS,
  );
  protected readonly actionClass = ACTION_CLASS;
  protected readonly closeClass = CLOSE_CLASS;
  protected readonly reopenClass = REOPEN_CLASS;

  protected specie = computed(() => SUPPLIER_TYPE_INFO[this.item().specie]);

  protected typeLabel = computed(() => this.item().purchaseTypeName);

  protected date = computed(() => formatIsoDate(this.item().date));
}
