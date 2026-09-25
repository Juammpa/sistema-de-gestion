import { Component, computed, input, output } from '@angular/core';
import { CashDay } from '../../interfaces/cash.interface';
import { formatCashDayTitle, paymentMethodLabel } from '../../utils/cash-utils';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'cash-day-modal',
  imports: [CurrencyPipe],
  templateUrl: './cash-day-modal.html',
})
export class CashDayModal {
  // Input
  day = input.required<CashDay>();

  // Output
  cancel = output();

  title = computed(() => formatCashDayTitle(this.day().date));
  methodLabel = paymentMethodLabel;

  // Cierra el modal
  close() {
    this.cancel.emit();
  }
}
