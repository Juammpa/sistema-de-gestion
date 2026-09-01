import { Component, input } from '@angular/core';
import { ListItem } from '../../interfaces/list-item.interface';

@Component({
  selector: 'search-list',
  imports: [],
  templateUrl: './search-list.html',
})
export class SearchList {
  // Almacena la lista a iterar
  searchList = input.required<ListItem[]>();
}
