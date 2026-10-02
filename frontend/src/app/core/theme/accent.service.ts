import { DOCUMENT, effect, inject, Injectable, signal } from '@angular/core';

export type AccentId = 'blue' | 'emerald' | 'violet' | 'amber';

const STORAGE_KEY = 'accent';
const ACCENT_IDS: AccentId[] = ['blue', 'emerald', 'violet', 'amber'];

@Injectable({ providedIn: 'root' })
export class AccentService {
  private document = inject(DOCUMENT);
  private _accent = signal<AccentId>(this.readSaved());

  readonly accent = this._accent.asReadonly();

  constructor() {
    // El CSS (theme-accent.css) reacciona a este atributo
    effect(() => {
      this.document.documentElement.dataset['accent'] = this._accent();
    });
  }

  setAccent(accent: AccentId) {
    this._accent.set(accent);
    try {
      localStorage.setItem(STORAGE_KEY, accent);
    } catch {
      // Sin almacenamiento (modo privado): el acento vale solo para esta sesion.
    }
  }

  private readSaved(): AccentId {
    try {
      const saved = localStorage.getItem(STORAGE_KEY) as AccentId | null;
      return saved && ACCENT_IDS.includes(saved) ? saved : 'blue';
    } catch {
      return 'blue';
    }
  }
}
