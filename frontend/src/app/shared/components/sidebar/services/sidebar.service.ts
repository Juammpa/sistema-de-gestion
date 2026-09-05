import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SidebarService {
  // Variable de estado
  isCollapsed = signal(false);

  collapse() {
    this.isCollapsed.set(true);
  }

  expand() {
    this.isCollapsed.set(false);
  }

  toggle() {
    this.isCollapsed.update((v) => !v);
  }
}
