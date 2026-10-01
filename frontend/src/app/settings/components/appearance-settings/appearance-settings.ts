import { Component, signal } from '@angular/core';

type ThemeId = 'light' | 'dark' | 'system';
type AccentId = 'blue' | 'emerald' | 'purple' | 'amber';

@Component({
  selector: 'appearance-settings',
  templateUrl: './appearance-settings.html',
  host: { class: 'block' },
})
export class AppearanceSettings {
  protected readonly themes: { id: ThemeId; label: string; icon: string }[] = [
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
      id: 'purple',
      label: 'Violeta',
      classes: 'bg-violet-400 text-white aria-pressed:ring-violet-400',
    },
    {
      id: 'amber',
      label: 'Ámbar',
      classes: 'bg-amber-500 text-amber-950 aria-pressed:ring-amber-500',
    },
  ];

  // Seleccion actual (por ahora solo visual)
  protected selectedTheme = signal<ThemeId>('dark');
  protected selectedAccent = signal<AccentId>('blue');
}
