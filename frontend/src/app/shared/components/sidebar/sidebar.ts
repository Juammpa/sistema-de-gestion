import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
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

  // En pantallas chicas (< md) el menú es un drawer que se abre con el botón de la barra superior
  protected isMobile = toSignal(
    inject(BreakpointObserver)
      .observe('(max-width: 767px)')
      .pipe(map((state) => state.matches)),
    { initialValue: false },
  );
  protected mobileOpen = signal(false);
  protected drawerOpen = computed(() => this.isMobile() && this.mobileOpen());
  // En móvil el drawer siempre se muestra expandido, aunque la página pida el menú colapsado
  protected collapsed = computed(() => !this.isMobile() && this.sidebarService.isCollapsed());

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
