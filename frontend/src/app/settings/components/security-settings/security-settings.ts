import { Component } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';

@Component({
  selector: 'security-settings',
  templateUrl: './security-settings.html',
  host: { class: 'block' },
})
export class SecuritySettings {
  protected readonly ui = SETTINGS_UI;

  // Datos de ejemplo: mas adelante vendran del backend
  protected readonly sessions = [
    {
      device: 'Chrome · Windows',
      icon: 'fa-solid fa-desktop',
      location: 'Concordia, Entre Ríos',
      lastActive: 'Activa ahora',
      current: true,
    },
    {
      device: 'App · iPhone',
      icon: 'fa-solid fa-mobile-screen',
      location: 'Concordia, Entre Ríos',
      lastActive: 'Hace 2 días',
      current: false,
    },
  ];
}
