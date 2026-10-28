import { Component, computed, input, model } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { AvailableCut } from '../../../../stock/interfaces/stock.interface';
import { selectedCents } from '../../utils/sale-units.utils';

@Component({
  selector: 'sale-units-picker',
  imports: [DecimalPipe, DatePipe],
  templateUrl: './sale-units-picker.html',
})
export class SaleUnitsPicker {
  // Unidades entre las que se puede elegir (ya del corte de la venta, de la más antigua a la más nueva)
  units = input.required<AvailableCut[]>();

  // Para mostrar el importe estimado (el definitivo lo calcula el backend)
  pricePerKilo = input<number | null>(null);

  // Ids de las unidades elegidas
  selectedIds = model.required<number[]>();

  protected count = computed(() => this.selectedIds().length);

  protected totalCents = computed(() => selectedCents(this.units(), this.selectedIds()));

  // Centavos: kilos (en centésimas) x precio por kilo
  protected amountCents = computed(() => {
    const price = this.pricePerKilo();
    return price === null ? null : Math.round(this.totalCents() * price);
  });

  protected isSelected(id: number) {
    return this.selectedIds().includes(id);
  }

  protected toggle(id: number) {
    this.selectedIds.update((ids) =>
      ids.includes(id) ? ids.filter((selected) => selected !== id) : [...ids, id],
    );
  }

  // Elige las N más antiguas (la lista ya viene ordenada de la más antigua a la más nueva)
  protected setCount(count: number) {
    const clamped = Math.min(Math.max(count, 0), this.units().length);
    this.selectedIds.set(
      this.units()
        .slice(0, clamped)
        .map((unit) => unit.id),
    );
  }

  protected onCountInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const parsed = Number(input.value);

    if (input.value.trim() !== '' && Number.isInteger(parsed)) {
      this.setCount(parsed);
    }

    // Si lo escrito no era válido o se ajustó al máximo, el campo vuelve al valor real
    input.value = String(this.count());
  }
}
