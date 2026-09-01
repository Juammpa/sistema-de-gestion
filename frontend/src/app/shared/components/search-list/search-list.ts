import { Component, input } from '@angular/core';
import { Client } from '../../../clientes/interfaces/client.interface';

@Component({
  selector: 'search-list',
  imports: [],
  templateUrl: './search-list.html',
})
export class SearchList {
  // Almacena la lista a iterar
  searchList = input.required<Client[]>();
}
