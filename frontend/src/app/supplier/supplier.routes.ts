import { Routes } from '@angular/router';

export const supplierRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/supplier-page/supplier-page'),
  },
  // Ruta dinamica
  {
    path: ':id',
    loadComponent: () => import('./pages/supplier-info-page/supplier-info-page'),
  },
];

export default supplierRoutes;
