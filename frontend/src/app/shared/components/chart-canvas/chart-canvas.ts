import { afterRenderEffect, Component, ElementRef, input, viewChild } from '@angular/core';
import {
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  ChartConfiguration,
  Filler,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
} from 'chart.js';

Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  Filler,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
);
Chart.defaults.color = '#94a3b8';

@Component({
  selector: 'chart-canvas',
  template: `<canvas #canvas role="img" [attr.aria-label]="label()"></canvas>`,
  host: { class: 'relative block' }, // el alto lo define quien lo usa (ej. h-72)
})
export class ChartCanvas {
  config = input.required<ChartConfiguration<'line'> | ChartConfiguration<'bar'>>();
  label = input<string>('Gráfico');

  private canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

  constructor() {
    afterRenderEffect((onCleanup) => {
      const chart = new Chart(this.canvas().nativeElement, this.config() as ChartConfiguration);
      onCleanup(() => chart.destroy());
    });
  }
}
