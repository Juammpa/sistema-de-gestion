import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DebtRanking } from '../../interfaces/dashboard.interface';
import { CurrencyPipe } from '@angular/common';

type Tone = 'emerald' | 'rose';

const TONES: Record<Tone, { total: string; amount: string; bar: string }> = {
  emerald: { total: 'text-emerald-400', amount: 'text-emerald-400', bar: 'bg-emerald-500/70' },
  rose: { total: 'text-rose-400', amount: 'text-rose-400', bar: 'bg-rose-500/70' },
};

@Component({
  selector: 'ranking-card',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './ranking-card.html',
  host: { class: 'block h-full' },
})
export class RankingCard {
  title = input.required<string>();
  subtitle = input.required<string>();
  items = input.required<DebtRanking[]>();
  total = input.required<number>();
  linkLabel = input.required<string>(); // ej. 'Ver todos los clientes'
  tone = input<Tone>('emerald');
  emptyText = input<string>('Sin saldos pendientes');
  pathRoute = input.required<string>(); // ej. '/home/clientes'

  toneClasses = computed(() => TONES[this.tone()]);

  // Cada barra se mide contra el mayor saldo de la lista
  rows = computed(() => {
    const max = Math.max(...this.items().map((item) => item.balance), 1);
    return this.items().map((item) => ({ ...item, pct: (item.balance / max) * 100 }));
  });
}
