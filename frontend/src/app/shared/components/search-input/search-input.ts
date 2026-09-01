import { Component, input, output } from '@angular/core';

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
}
