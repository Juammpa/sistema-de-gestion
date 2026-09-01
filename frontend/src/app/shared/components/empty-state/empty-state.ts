import { Component, input } from '@angular/core';

@Component({
  selector: 'empty-state',
  imports: [],
  templateUrl: './empty-state.html',
})
export class EmptyState {
  //
  infoMessage = input.required<string>();
}
