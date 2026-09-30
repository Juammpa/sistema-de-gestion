import { Component, computed, input, output } from '@angular/core';
import { ApiError } from '../../../core/errors/api-error';

@Component({
  selector: 'error-state',
  templateUrl: './error-state.html',
})
export class ErrorState {
  title = input<string>('No se pudieron cargar los datos');
  error = input<unknown>(null);
  retry = output<void>();

  protected detail = computed(() => {
    const error = this.error();
    return error instanceof ApiError ? error.message : null;
  });

  // Un 404 no se arregla reintentando
  protected canRetry = computed(() => {
    const error = this.error();
    return !(error instanceof ApiError && error.status === 404);
  });
}
