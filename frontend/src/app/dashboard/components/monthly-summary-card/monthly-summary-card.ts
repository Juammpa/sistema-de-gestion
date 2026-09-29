import { CurrencyPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { MonthlyFlow } from '../../interfaces/dashboard.interface';
import { monthLong } from '../../utils/dashboard.utils';

@Component({
  selector: 'monthly-summary-card',
  imports: [CurrencyPipe],
  templateUrl: './monthly-summary-card.html',
  host: { class: 'block h-full' },
})
export class MonthlySummaryCard {
  month = input.required<MonthlyFlow>(); // el mes en curso

  title = computed(() => `${monthLong(this.month().month)} ${this.month().year}`);

  // Las barras se miden contra el mayor de los cuatro totales
  rows = computed(() => {
    const m = this.month();
    const max = Math.max(m.sales, m.purchases, m.collections, m.payments, 1);

    return [
      { label: 'Total ventas', value: m.sales, bar: 'bg-blue-500' },
      { label: 'Total compras', value: m.purchases, bar: 'bg-amber-500' },
      { label: 'Cobros efectivos', value: m.collections, bar: 'bg-emerald-500' },
      { label: 'Pagos realizados', value: m.payments, bar: 'bg-rose-500' },
    ].map((row) => ({ ...row, pct: (row.value / max) * 100 }));
  });

  // Margen bruto e indice de cobranza (null si todavia no hubo ventas en el mes)
  margin = computed(() => {
    const m = this.month();
    return m.sales > 0 ? ((m.sales - m.purchases) / m.sales) * 100 : null;
  });

  collectionRate = computed(() => {
    const m = this.month();
    return m.sales > 0 ? (m.collections / m.sales) * 100 : null;
  });
}
