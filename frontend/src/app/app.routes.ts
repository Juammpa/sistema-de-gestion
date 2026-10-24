import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./shared/pages/home-page/home-page').then((m) => m.HomePage),
  },
  {
    path: 'home',
    canActivate: [authGuard],
    loadChildren: () => import('./shared/shared.routes'),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
