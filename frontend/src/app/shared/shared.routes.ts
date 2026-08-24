import { Routes } from '@angular/router';
import { LayoutPage } from './pages/layout-page/layout-page';

export const Sharedroutes: Routes = [
  {
    path: '',
    component: LayoutPage,
  },
  {
    path: '**',
    redirectTo: '',
  },
];

export default Sharedroutes;
