import { Component, inject, signal } from '@angular/core';
import { ThemePreference, ThemeService } from '../../../core/theme/theme.service';
import { AccentId, AccentService } from '../../../core/theme/accent.service';

@Component({
  selector: 'appearance-settings',
  templateUrl: './appearance-settings.html',
  host: { class: 'block' },
})
export class AppearanceSettings {
  private themeService = inject(ThemeService);
  private accentService = inject(AccentService);

  protected readonly themes: { id: ThemePreference; label: string; icon: string }[] = [
    { id: 'light', label: 'Claro', icon: 'fa-regular fa-sun' },
    { id: 'dark', label: 'Oscuro', icon: 'fa-regular fa-moon' },
    { id: 'system', label: 'Sistema', icon: 'fa-solid fa-desktop' },
  ];

  protected readonly accents: { id: AccentId; label: string; classes: string }[] = [
    { id: 'blue', label: 'Azul', classes: 'bg-blue-500 text-white aria-pressed:ring-blue-500' },
    {
      id: 'emerald',
      label: 'Verde',
      classes: 'bg-emerald-500 text-white aria-pressed:ring-emerald-500',
    },
    {
      id: 'violet',
      label: 'Violeta',
      classes: 'bg-violet-400 text-white aria-pressed:ring-violet-400',
    },
    {
      id: 'amber',
      label: 'Ámbar',
      classes: 'bg-amber-500 text-amber-950 aria-pressed:ring-amber-500',
    },
  ];

  // Seleccion actual
  protected selectedTheme = this.themeService.preference;
  protected selectedAccent = this.accentService.accent;

  protected selectTheme(theme: ThemePreference) {
    this.themeService.setPreference(theme);
  }

  protected selectAccent(accent: AccentId) {
    this.accentService.setAccent(accent);
  }
}
