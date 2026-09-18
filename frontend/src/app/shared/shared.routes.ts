import { Routes } from '@angular/router';
import { LayoutPage } from './pages/layout-page/layout-page';

export const Sharedroutes: Routes = [
  {
    path: '',
    component: LayoutPage,

    children: [
      {
        path: 'clientes',
        loadChildren: () => import('../clientes/clientes.routes'),
      },
      {
        path: 'proveedores',
        loadChildren: () => import('../supplier/supplier.routes'),
      },
      {
        path: 'cortes',
        loadComponent: () => import('../cut-meat/pages/cut-meat-page/cut-meat-page'),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];

export default Sharedroutes;
