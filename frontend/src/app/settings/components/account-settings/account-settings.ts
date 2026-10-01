import { Component } from '@angular/core';
import { SETTINGS_UI } from '../../utils/settings-ui';

@Component({
  selector: 'account-settings',
  templateUrl: './account-settings.html',
  host: { class: 'block' },
})
export class AccountSettings {
  protected readonly ui = SETTINGS_UI;
}
