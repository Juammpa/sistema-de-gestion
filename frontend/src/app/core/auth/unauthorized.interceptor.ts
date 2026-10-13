import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import Keycloak from 'keycloak-js';
import { catchError, throwError } from 'rxjs';

// Ventana minima entre redirecciones al login: corta el bucle si el 401 persiste tras volver a iniciar sesion.
const LAST_REDIRECT_KEY = 'last-login-redirect';
const LOOP_GUARD_MS = 10_000;

// Evita un bucle infinito de redirecciones si el 401 persiste despues de volver a iniciar sesion
function tryAcquireLoginRedirect(): boolean {
  try {
    const last = Number(sessionStorage.getItem(LAST_REDIRECT_KEY) ?? 0);

    if (Date.now() - last < LOOP_GUARD_MS) return false;
    sessionStorage.setItem(LAST_REDIRECT_KEY, String(Date.now()));
  } catch {
    // Sin acceso al storage no se puede proteger: se permite la redireccion.
  }

  return true;
}

// Un 401 de la API significa que el token ya no sirve: se vuelve a iniciar sesion
export const unauthorizedInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(Keycloak);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && tryAcquireLoginRedirect()) {
        keycloak.login().catch(() => undefined);
      }
      return throwError(() => error);
    }),
  );
};
