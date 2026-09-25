import { Component, computed, input, output } from '@angular/core';
import { CashDay } from '../../interfaces/cash.interface';
import { CurrencyPipe } from '@angular/common';
import { formatCashDayTitle } from '../../utils/cash-utils';

@Component({
  selector: 'cash-item',
  imports: [CurrencyPipe],
  templateUrl: './cash-item.html',
})
export class CashItem {
  // Input
  day = input.required<CashDay>();

  // Output --> El usuario quiere ver el detalle del dia
  open = output();

  // "Miercoles 9/9/2026"
  title = computed(() => formatCashDayTitle(this.day().date));
}
