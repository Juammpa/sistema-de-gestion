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

  // Emite que filtro cambio y a que valor
  onFilterChange(key: string, event: Event) {
    this.filterChange.emit({ key, value: (event.target as HTMLSelectElement).value });
  }
}
