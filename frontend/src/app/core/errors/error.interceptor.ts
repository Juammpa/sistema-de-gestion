import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, delay, retry, throwError, timer } from 'rxjs';
import { isRetryable, toApiError } from './api-error';
import { ERROR_NOTIFICATION } from './error-context';
import { NotificationService } from '../notifications/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);
  const isRead = req.method === 'GET';

  return next(req).pipe(
    // Solo lecturas: son idempotentes, reintentar no puede duplicar nada
    retry({
      count: isRead ? 1 : 0,
      delay: (error) => (isRetryable(error) ? timer(600) : throwError(() => error)),
    }),
    catchError((error: unknown) => {
      const apiError = toApiError(error);
      const mode = req.context.get(ERROR_NOTIFICATION);

      if (mode === 'always' || (mode === 'auto' && !isRead)) {
        notifications.error(apiError.message);
      }

      // Se relanza ya convertido: los suscribe y rxResource reciben un ApiError
      return throwError(() => apiError);
    }),
  );
};
