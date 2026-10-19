import { Component, inject, signal } from '@angular/core';
import { ProfileSettings } from '../../components/profile-settings/profile-settings';
import { AppearanceSettings } from '../../components/appearance-settings/appearance-settings';
import { SecuritySettings } from '../../components/security-settings/security-settings';
import { AccountSettings } from '../../components/account-settings/account-settings';
import { ActivatedRoute } from '@angular/router';

type SectionId = 'profile' | 'appearance' | 'security' | 'account';

// Valores aceptados en la direccion (?seccion=...) para abrir una seccion en particular
const SECTION_BY_QUERY: Record<string, SectionId> = {
  perfil: 'profile',
  apariencia: 'appearance',
  seguridad: 'security',
  cuenta: 'account',
};

@Component({
  selector: 'app-settings-page',
  imports: [ProfileSettings, AppearanceSettings, SecuritySettings, AccountSettings],
  templateUrl: './settings-page.html',
  host: { class: 'block h-full min-h-0 flex-1 overflow-y-auto scrollbar-none' },
})
export default class SettingsPage {
  private route = inject(ActivatedRoute);

  sections: { id: SectionId; label: string; icon: string }[] = [
    { id: 'profile', label: 'Perfil', icon: 'fa-regular fa-user' },
    { id: 'appearance', label: 'Apariencia', icon: 'fa-solid fa-circle-half-stroke' },
    { id: 'security', label: 'Seguridad', icon: 'fa-solid fa-lock' },
    { id: 'account', label: 'Cuenta', icon: 'fa-solid fa-user-gear' },
  ];

  // Seccion visible: la que pide la direccion (ej. volver de Keycloak) o Perfil
  selected = signal<SectionId>(this.initialSection());

  private initialSection(): SectionId {
    const requested = this.route.snapshot.queryParamMap.get('seccion') ?? '';

    return SECTION_BY_QUERY[requested] ?? 'profile';
  }
}
