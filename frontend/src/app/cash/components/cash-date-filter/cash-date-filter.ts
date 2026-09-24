import { Component, model } from '@angular/core';

@Component({
  selector: 'cash-date-filter',
  templateUrl: './cash-date-filter.html',
  host: { class: 'block' },
})
export class CashDateFilter {
  // Fechas en formato yyyy-MM-dd
  from = model.required<string>();
  to = model.required<string>();

  // Actualiza 'from' solo si el usuario eligio una fecha valida
  onFromChange(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    if (value) this.from.set(value);
  }

  // Actualiza 'to' solo si el usuario eligio una fecha valida
  onToChange(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    if (value) this.to.set(value);
  }

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
