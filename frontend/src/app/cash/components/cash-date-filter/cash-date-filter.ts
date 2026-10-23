import { Component, model } from '@angular/core';
import { DateField } from '../../../shared/components/date-field/date-field';

@Component({
  selector: 'cash-date-filter',
  imports: [DateField],
  templateUrl: './cash-date-filter.html',
  host: { class: 'block' },
})
export class CashDateFilter {
  // Fechas en formato yyyy-MM-dd
  from = model.required<string>();
  to = model.required<string>();

  // Atajo: solo el dia de hoy
  setToday() {
    this.setLastDays(1);
  }

  // Atajo: los ultimos 'days' dias, incluyendo hoy
  setLastDays(days: number) {
    const start = new Date();
    start.setDate(start.getDate() - (days - 1));
    this.from.set(this.toIsoDate(start));
    this.to.set(this.toIsoDate(new Date()));
  }

  // Formatea una fecha como yyyy-MM-dd, en horario local
  private toIsoDate(date: Date): string {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
