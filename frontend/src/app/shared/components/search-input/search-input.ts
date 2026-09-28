import { Component, input, output } from '@angular/core';
import { FilterChange, SearchFilter } from '../../interfaces/search-filter.interface';

@Component({
  selector: 'search-input',
  imports: [],
  templateUrl: './search-input.html',
})
export class SearchInput {
  // Inputs
  placeholder = input.required<string>();
  numberFound = input.required<number | undefined>();
  searchName = input.required<string>();
  filters = input<SearchFilter[]>([]); // Cada pagina define los suyos

  // Output
  add = output();
  filterChange = output<FilterChange>();
  searchChange = output<string>(); // Texto escrito en el buscador

  // Emite que filtro cambio y a que valor
  onFilterChange(key: string, event: Event) {
    this.filterChange.emit({ key, value: (event.target as HTMLSelectElement).value });
  }

  // Emite el texto cada vez que el usuario escribe (o lo borra con la X del navegador)
  onSearch(event: Event) {
    this.searchChange.emit((event.target as HTMLInputElement).value);
  }
}
