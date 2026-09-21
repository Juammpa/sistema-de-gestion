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

  onFromChange(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    if (value) this.from.set(value);
  }

  onToChange(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    if (value) this.to.set(value);
  }
}
