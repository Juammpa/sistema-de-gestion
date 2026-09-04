import { Component, input, output } from '@angular/core';
import { ListItem } from '../../interfaces/list-item.interface';
import { ListItemCard } from '../list-item-card/list-item-card';

@Component({
  selector: 'search-list',
  imports: [ListItemCard],
  templateUrl: './search-list.html',
})
export class SearchList {
  // Almacena la lista a iterar
  searchList = input.required<ListItem[]>();
  pathRoute = input.required<string>(); // ej. 'clientes' o 'proveedores'

  // Output
  itemClick = output<ListItem>();
}
