import { CurrencyPipe } from '@angular/common';
import { Component, input } from '@angular/core';

@Component({
  selector: 'cash-header',
  imports: [CurrencyPipe],
  templateUrl: './cash-header.html',
})
export class CashHeader {
  // Saldo actual de la caja (undefined mientras carga o si falla)
  balance = input<number | null | undefined>();
}
