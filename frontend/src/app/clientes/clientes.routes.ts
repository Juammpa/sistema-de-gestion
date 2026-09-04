import { Routes } from '@angular/router';

export const clientesRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/client-page/client-page'),
  },
  // Ruta dinamica
  {
    path: ':id',
    loadComponent: () => import('./pages/client-info-page/client-info-page'),
  },
];

export default clientesRoutes;
