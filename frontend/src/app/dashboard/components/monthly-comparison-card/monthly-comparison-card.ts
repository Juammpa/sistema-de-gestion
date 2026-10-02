import { Component, computed, inject, input } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { ChartCanvas } from '../../../shared/components/chart-canvas/chart-canvas';
import { MonthlyFlow } from '../../interfaces/dashboard.interface';
import { baseChartOptions } from '../../utils/chart-theme';
import { monthShort } from '../../utils/dashboard.utils';
import { ThemeService } from '../../../core/theme/theme.service';

@Component({
  selector: 'monthly-comparison-card',
  imports: [ChartCanvas],
  templateUrl: './monthly-comparison-card.html',
  host: { class: 'block h-full' },
})
export class MonthlyComparisonCard {
  private theme = inject(ThemeService);

  months = input.required<MonthlyFlow[]>();

  config = computed<ChartConfiguration<'bar'>>(() => ({
    type: 'bar',
    data: {
      labels: this.months().map((m) => monthShort(m.month)),
      datasets: [
        {
          label: 'Ventas',
          data: this.months().map((m) => m.sales),
          backgroundColor: '#3b82f6',
          borderRadius: 4,
          maxBarThickness: 28,
        },
        {
          label: 'Compras',
          data: this.months().map((m) => m.purchases),
          backgroundColor: '#f59e0b',
          borderRadius: 4,
          maxBarThickness: 28,
        },
      ],
    },
    options: baseChartOptions<'bar'>(this.theme.resolved()),
  }));
}
