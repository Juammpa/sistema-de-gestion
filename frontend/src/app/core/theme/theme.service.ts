import { computed, DOCUMENT, effect, inject, Injectable, signal } from '@angular/core';

export type ThemePreference = 'light' | 'dark' | 'system';
export type ResolvedTheme = 'light' | 'dark';

const STORAGE_KEY = 'theme';

// Color de la barra del navegador (movil) segun el tema
const BROWSER_COLORS: Record<ResolvedTheme, string> = { dark: '#0a0f1d', light: '#f4f2ee' };

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private document = inject(DOCUMENT);
  private systemQuery = window.matchMedia('(prefers-color-scheme:dark)');

  private systemDark = signal(this.systemQuery.matches);
  private _preference = signal<ThemePreference>(this.readSaved());

  // Lo que eligio el usuario (claro, oscuro o sistema)
  readonly preference = this._preference.asReadonly();

  // El tema que realmente se esta mostrando
  readonly resolved = computed<ResolvedTheme>(() => {
    const preference = this._preference();
    if (preference === 'system') return this.systemDark() ? 'dark' : 'light';
    return preference;
  });

  constructor() {
    // Si el sistema operativo cambia de tema, se actualiza (solo importa en modo "sistema")
    this.systemQuery.addEventListener('change', (event) => this.systemDark.set(event.matches));

    effect(() => {
      const theme = this.resolved();
      this.document.documentElement.dataset['theme'] = theme;
      this.document
        .querySelector('meta[name="theme-color"]')
        ?.setAttribute('content', BROWSER_COLORS[theme]);
      this.document.querySelector('meta[name="color-scheme"]')?.setAttribute('content', theme);
    });
  }

  setPreference(preference: ThemePreference) {
    this._preference.set(preference);
    try {
      localStorage.setItem(STORAGE_KEY, preference);
    } catch {
      // Sin almacenamiento (modo privado): el tema vale solo para esta sesion.
    }
  }

  private readSaved(): ThemePreference {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved === 'light' || saved === 'dark' || saved === 'system' ? saved : 'dark';
    } catch {
      return 'dark';
    }
  }
}
