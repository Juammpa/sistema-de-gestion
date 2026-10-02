import { CurrencyPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';

type Tone = 'emerald' | 'rose' | 'amber';

// Clases completas (Tailwind no detecta nombres armados con texto)
const TONES: Record<Tone, { card: string; dot: string }> = {
  emerald: {
    card: 'border-emerald-500/25 from-emerald-950/50 to-panel-to',
    dot: 'bg-emerald-400',
  },
  rose: {
    card: 'border-rose-500/25 from-rose-950/50 to-panel-to',
    dot: 'bg-rose-400',
  },
  amber: {
    card: 'border-amber-500/25 from-amber-950/40 to-panel-to',
    dot: 'bg-amber-400',
  },
};

@Component({
  selector: 'kpi-card',
  imports: [CurrencyPipe],
  templateUrl: './kpi-card.html',
})
export class KpiCard {
  label = input.required<string>();
  featured = input<boolean>(false); // la cifra principal del panel: más grande
  value = input.required<number>();
  subtitle = input<string>('');
  tone = input<Tone>('emerald');
  change = input<number | null>(null); // variacion % vs mes anterior
  invertTrend = input<boolean>(false); // true: que suba es malo (ej. por pagar)

  toneClasses = computed(() => TONES[this.tone()]);

  trend = computed(() => {
    const change = this.change();
    if (change === null) return null;

    const up = change >= 0;
    return {
      up,
      good: up !== this.invertTrend(),
      text: Math.abs(change).toLocaleString('es-AR', { maximumFractionDigits: 1 }),
    };
  });
}
