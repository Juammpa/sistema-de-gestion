import { Component, input } from '@angular/core';

@Component({
  selector: 'empty-state',
  imports: [],
  templateUrl: './empty-state.html',
})
export class EmptyState {
  // Inputs
  infoMessage = input.required<string>();
  icon = input<string>('fa-solid fa-users');
}
