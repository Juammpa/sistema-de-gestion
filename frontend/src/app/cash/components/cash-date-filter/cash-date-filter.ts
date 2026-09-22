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
}
