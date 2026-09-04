import { Routes } from '@angular/router';

export const supplierRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/supplier-page/supplier-page'),
  },
  // Ruta dinamica
  // {
  //   path: ':id',
  //   loadComponent: () => import('./pages/client-info-page/client-info-page'),
  // },
];

export default supplierRoutes;
