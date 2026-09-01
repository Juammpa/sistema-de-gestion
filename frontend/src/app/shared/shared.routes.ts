import { Routes } from '@angular/router';
import { LayoutPage } from './pages/layout-page/layout-page';

export const Sharedroutes: Routes = [
  {
    path: '',
    component: LayoutPage,

    children: [
      {
        path: 'clientes',
        loadComponent: () => import('../clientes/pages/client-page/client-page'),
      },
      {
        path: 'proveedores',
        loadComponent: () => import('../supplier/pages/supplier-page/supplier-page'),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];

export default Sharedroutes;
