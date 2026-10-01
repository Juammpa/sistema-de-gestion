import { Component, signal } from '@angular/core';
import { ProfileSettings } from '../../components/profile-settings/profile-settings';
import { AppearanceSettings } from '../../components/appearance-settings/appearance-settings';
import { SecuritySettings } from '../../components/security-settings/security-settings';
import { AccountSettings } from '../../components/account-settings/account-settings';

type SectionId = 'profile' | 'appearance' | 'security' | 'account';

@Component({
  selector: 'app-settings-page',
  imports: [ProfileSettings, AppearanceSettings, SecuritySettings, AccountSettings],
  templateUrl: './settings-page.html',
  host: { class: 'block h-full min-h-0 flex-1 overflow-y-auto scrollbar-none' },
})
export default class SettingsPage {
  sections: { id: SectionId; label: string; icon: string }[] = [
    { id: 'profile', label: 'Perfil', icon: 'fa-regular fa-user' },
    { id: 'appearance', label: 'Apariencia', icon: 'fa-solid fa-circle-half-stroke' },
    { id: 'security', label: 'Seguridad', icon: 'fa-solid fa-lock' },
    { id: 'account', label: 'Cuenta', icon: 'fa-solid fa-user-gear' },
  ];

  // Seccion visible
  selected = signal<SectionId>('profile');
}
