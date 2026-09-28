import { Component, input, model } from '@angular/core';
import { MovementFilterOption, MovementType } from '../../interfaces/movement.interface';

@Component({
  selector: 'movements-filters',
  imports: [],
  templateUrl: './movements-filters.html',
})
export class MovementsFilters {
  // Botones de tipo de movimiento (los define cada pagina)
  options = input.required<MovementFilterOption[]>();

  // Tipo elegido (null = todos). Se usa con [(selectedType)]
  selectedType = model<MovementType | null>(null);
}
