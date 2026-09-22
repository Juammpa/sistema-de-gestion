import { Component, inject, signal } from '@angular/core';
import { CashService } from '../../services/cash.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { CashItem } from '../../components/cash-item/cash-item';
import { CashDateFilter } from '../../components/cash-date-filter/cash-date-filter';

@Component({
  selector: 'app-cash-page',
  imports: [LoadingSpinner, EmptyState, CashItem, CashDateFilter],
  templateUrl: './cash-page.html',
})
export default class CashPage {
  private cashService = inject(CashService);

  // Por defecto: los ultimos 7 dias.
  to = signal<string>(this.toIsoDate(new Date()));
  from = signal<string>(this.toIsoDate(this.dayAgo(6)));

  cashResource = rxResource({
    params: () => ({ from: this.from(), to: this.to() }),
    stream: ({ params }) => this.cashService.getCash(params.from, params.to),
  });

  // Fecha de hace 'days' dias
  private dayAgo(days: number): Date {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  }

  // Formatea una fecha como yyyy-MM-dd, en horario local
  private toIsoDate(date: Date): string {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
