import { Routes } from '@angular/router';
import { LayoutPage } from './pages/layout-page/layout-page';
import { adminGuard } from '../core/auth/admin.guard';

export const Sharedroutes: Routes = [
  {
    path: '',
    component: LayoutPage,

    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'inicio',
      },
      {
        path: 'inicio',
        loadComponent: () => import('../dashboard/pages/dashboard-page/dashboard-page'),
      },
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
      {
        path: 'caja-ahorro',
        loadComponent: () => import('../cash/pages/cash-page/cash-page'),
      },
      {
        path: 'stock',
        loadComponent: () => import('../stock/pages/stock-page/stock-page'),
      },
      {
        path: 'usuarios',
        canMatch: [adminGuard], // Solo ADMIN
        loadComponent: () => import('../users/pages/users-page/users-page'),
      },
      {
        path: 'ajustes',
        loadComponent: () => import('../settings/pages/settings-page/settings-page'),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];

export default Sharedroutes;
