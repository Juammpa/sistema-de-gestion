import { CurrencyPipe, TitleCasePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import translateSpecie from '../../utils/specie.utils';

@Component({
  selector: 'cut-meat-list-item',
  imports: [CurrencyPipe, TitleCasePipe],
  templateUrl: './cut-meat-list-item.html',
})
export class CutMeatListItem {
  translateSpecie = translateSpecie;

  // Inputs
  cutMeatName = input.required<string>();
  specie = input.required<string>();
  price = input.required<number>();
  isActive = input.required<boolean>();

  // Colores del bagde segun la especie
  specieBadgeClass = computed(() =>
    this.specie() === 'PORCINE'
      ? 'border-rose-500/30 bg-rose-500/10 text-rose-300'
      : 'border-amber-500/90 bg-amber-500/10 text-amber-300',
  );
}
