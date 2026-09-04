import { Component, input, output } from '@angular/core';
import { ListItem } from '../../interfaces/list-item.interface';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'list-item-card',
  imports: [RouterLink],
  templateUrl: './list-item-card.html',
})
export class ListItemCard {
  // Inputs
  item = input.required<ListItem>();
  pathRoute = input.required<string[]>(); // ej. 'clientes/{id}' o 'proveedores/{id}

  // Outputs
  itemClick = output<ListItem>();
}
