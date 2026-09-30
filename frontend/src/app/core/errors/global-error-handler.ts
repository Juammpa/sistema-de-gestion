import { ErrorHandler, inject } from '@angular/core';
import { ApiError } from './api-error';
import { NotificationService } from '../notifications/notification.service';

export class GlobalErrorHandler implements ErrorHandler {
  private notifications = inject(NotificationService);

  handleError(error: unknown) {
    // Los errores HTTP ya se informaron (toast o pantalla). Aca solo llegan bugs.
    if (error instanceof ApiError) return;

    console.error(error);
    this.notifications.error('Ocurrió un error inesperado. Si se repite, recargá la página.');
  }
}
