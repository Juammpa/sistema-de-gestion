import { Component, computed, input, signal } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { ChartCanvas } from '../../../shared/components/chart-canvas/chart-canvas';
import { MonthlyFlow } from '../../interfaces/dashboard.interface';
import { baseChartOptions, crosshairPlugin } from '../../utils/chart-theme';
import { monthShort } from '../../utils/dashboard.utils';

type FlowMode = 'cash' | 'trade';

@Component({
  selector: 'flow-chart-card',
  imports: [ChartCanvas],
  templateUrl: './flow-chart-card.html',
  host: { class: 'block h-full' },
})
export class FlowChartCard {
  months = input.required<MonthlyFlow[]>();

  modes: { value: FlowMode; label: string }[] = [
    { value: 'cash', label: 'Cobros / Pagos' },
    { value: 'trade', label: 'Ventas / Compras' },
  ];
  mode = signal<FlowMode>('cash');

  // Las dos series segun el modo elegido
  series = computed(() => {
    const months = this.months();

    return this.mode() === 'cash'
      ? [
          { label: 'Cobros', color: '#10b981', data: months.map((m) => m.collections) },
          { label: 'Pagos', color: '#f43f5e', data: months.map((m) => m.payments) },
        ]
      : [
          { label: 'Ventas', color: '#3b82f6', data: months.map((m) => m.sales) },
          { label: 'Compras', color: '#f59e0b', data: months.map((m) => m.purchases) },
        ];
  });

  config = computed<ChartConfiguration<'line'>>(() => ({
    type: 'line',
    data: {
      labels: this.months().map((m) => monthShort(m.month)),
      datasets: this.series().map((serie) => ({
        label: serie.label,
        data: serie.data,
        borderColor: serie.color,
        backgroundColor: serie.color + '1f', // mismo color con transparencia
        fill: true,
        tension: 0.4,
        borderWidth: 2,
        pointRadius: 0,
        pointHoverRadius: 5,
        pointBackgroundColor: '#ffffff',
        pointBorderColor: serie.color,
      })),
    },
    options: baseChartOptions<'line'>(),
    plugins: [crosshairPlugin],
  }));

  chartLabel = computed(
    () =>
      `Evolución mensual de ${this.series()
        .map((s) => s.label.toLowerCase())
        .join(' y ')}, últimos 12 meses`,
  );
}
