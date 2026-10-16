import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

// Solo deja entrar a administradores
export const adminGuard: CanMatchFn = () => {
  const auth = inject(AuthService);

  return auth.isAdmin ? true : inject(Router).createUrlTree(['/home/inicio']);
};
