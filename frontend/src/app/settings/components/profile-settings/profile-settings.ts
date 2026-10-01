import { Component } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';

@Component({
  selector: 'profile-settings',
  templateUrl: './profile-settings.html',
  host: { class: 'block' },
})
export class ProfileSettings {
  protected readonly ui = SETTINGS_UI;

  // Datos de ejemplo: mas adelante vendran del usuario real.
  protected readonly user = {
    name: 'Juan',
    lastName: 'Menoyo',
    email: 'juanmenoyo11@gmail.com',
    phone: '3442 501598',
  };

  protected readonly initials = `${this.user.name[0]}${this.user.lastName[0]}`;
}
