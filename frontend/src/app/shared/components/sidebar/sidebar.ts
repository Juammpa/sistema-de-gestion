import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SidebarService } from './services/sidebar.service';
import { MenuItem } from './interfaces/sidebar.interface';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './sidebar.html',
})
export class Sidebar {
  sidebarService = inject(SidebarService);

  principalItems: MenuItem[] = [
    {
      name: 'Inicio',
      route: 'inicio',
      icon: 'fa-solid fa-house',
    },
    {
      name: 'Clientes',
      route: 'clientes',
      icon: 'fa-solid fa-users',
    },
    {
      name: 'Proveedores',
      route: 'proveedores',
      icon: 'fa-solid fa-cart-shopping',
    },
    {
      name: 'Stock',
      route: 'stock',
      icon: 'fa-solid fa-database',
    },
  ];

  managementItems: MenuItem[] = [
    {
      name: 'Cortes',
      route: 'cortes',
      icon: 'fa-solid fa-cow',
    },
    {
      name: 'Caja Ahorro',
      route: 'caja-ahorro',
      icon: 'fa-solid fa-dollar-sign',
    },
    {
      name: 'Ajustes',
      route: 'ajustes',
      icon: 'fa-solid fa-gear',
    },
  ];
}
