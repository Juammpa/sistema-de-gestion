import { CurrencyPipe } from '@angular/common';
import { Component, input } from '@angular/core';

@Component({
  selector: 'cut-meat-list-item',
  imports: [CurrencyPipe],
  templateUrl: './cut-meat-list-item.html',
})
export class CutMeatListItem {
  // Inputs
  cutMeatName = input.required<string>();
  specie = input.required<string>();
  price = input.required<number>();
}
