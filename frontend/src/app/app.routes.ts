import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./shared/pages/home-page/home-page').then((m) => m.HomePage),
  },
  {
    path: 'home',
    loadChildren: () => import('./shared/shared.routes'),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
