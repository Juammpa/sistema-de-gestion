import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';

@Component({
  selector: 'stock-header',
  imports: [DecimalPipe],
  templateUrl: './stock-header.html',
})
export class StockHeader {
  // Totales del periodo (undefined mientras carga o si falla)
  kilograms = input<number | null | undefined>();
  units = input<number | null | undefined>();
}
